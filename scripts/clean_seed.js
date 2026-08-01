const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const xlsx = require('xlsx');
const path = require('path');
const fs = require('fs');

const serviceAccountPath = path.join(__dirname, '../config/grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error(`Service account key not found at ${serviceAccountPath}`);
  process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

function slugify(text) {
  return text.toString().toLowerCase()
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^\w\-]+/g, '')
    .replace(/\-\-+/g, '_');
}

async function purgeCollection(collectionPath) {
  const snapshot = await db.collection(collectionPath).get();
  const batch = db.batch();
  snapshot.docs.forEach(doc => batch.delete(doc.ref));
  await batch.commit();
}

async function cleanAndSeed() {
  console.log('--- Cleaning old records and re-seeding cleanly ---');

  // Purge old collections
  await purgeCollection('items');
  await purgeCollection('branches/branch_1/menu_items');
  await purgeCollection('branches/branch_2/menu_items');
  await purgeCollection('categories');

  console.log('✓ Old collections purged successfully.');

  const excelPath = '/tmp/menu_sheet.xlsx';
  const workbook = xlsx.readFile(excelPath);

  // 1. Seed Branches
  const initialBranches = [
    { id: 'branch_1', name: 'Branch 1', code: 'BR1', isActive: true, createdAt: new Date() },
    { id: 'branch_2', name: 'Branch 2', code: 'BR2', isActive: true, createdAt: new Date() }
  ];

  for (const branch of initialBranches) {
    await db.collection('branches').doc(branch.id).set(branch, { merge: true });
  }

  // 2. Seed Categories
  const categoriesSheet = workbook.Sheets['Categories'];
  const categoriesData = xlsx.utils.sheet_to_json(categoriesSheet, { header: 1 });
  let categoryOrder = 1;

  for (let i = 1; i < categoriesData.length; i++) {
    const row = categoriesData[i];
    if (row && row[0]) {
      const catName = row[0].toString().trim();
      const catId = slugify(catName);
      await db.collection('categories').doc(catId).set({
        id: catId,
        name: catName,
        displayOrder: categoryOrder++,
        isActive: true,
        updatedAt: new Date()
      });
    }
  }

  // 3. Seed Items (Master + Subcollections)
  const masterSheet = workbook.Sheets['Items (Master)'];
  const masterRows = xlsx.utils.sheet_to_json(masterSheet, { header: 1 });

  let currentCatName = 'General';
  let itemOrder = 1;
  let batch = db.batch();
  let opCount = 0;
  let itemCounter = 0;

  for (let i = 1; i < masterRows.length; i++) {
    const row = masterRows[i];
    if (!row || row.length === 0) continue;

    let [cat, itemName, defaultPrice, b1Price, b2Price, b1Avail, b2Avail] = row;

    if (cat && cat.toString().trim()) {
      currentCatName = cat.toString().trim();
    }

    if (!itemName || !itemName.toString().trim()) continue;

    const name = itemName.toString().trim();
    const catId = slugify(currentCatName);
    const rawId = slugify(name);
    const itemId = `${catId}_${rawId}`;

    const price = parseFloat(defaultPrice) || 0;
    const branch1Price = parseFloat(b1Price) || price;
    const branch2Price = parseFloat(b2Price) || price;
    const branch1Available = (b1Avail === true || b1Avail === 'TRUE' || b1Avail === 'true' || b1Avail === 1);
    const branch2Available = (b2Avail === true || b2Avail === 'TRUE' || b2Avail === 'true' || b2Avail === 1);

    // Master
    batch.set(db.collection('items').doc(itemId), {
      id: itemId,
      name: name,
      categoryId: catId,
      categoryName: currentCatName,
      defaultPrice: price,
      displayOrder: itemOrder,
      createdAt: new Date(),
      updatedAt: new Date()
    });
    opCount++;

    // Branch 1
    batch.set(db.collection('branches').doc('branch_1').collection('menu_items').doc(itemId), {
      itemId: itemId,
      name: name,
      categoryId: catId,
      categoryName: currentCatName,
      price: branch1Price,
      isAvailable: branch1Available,
      displayOrder: itemOrder,
      updatedAt: new Date()
    });
    opCount++;

    // Branch 2
    batch.set(db.collection('branches').doc('branch_2').collection('menu_items').doc(itemId), {
      itemId: itemId,
      name: name,
      categoryId: catId,
      categoryName: currentCatName,
      price: branch2Price,
      isAvailable: branch2Available,
      displayOrder: itemOrder,
      updatedAt: new Date()
    });
    opCount++;

    itemOrder++;
    itemCounter++;

    if (opCount >= 300) {
      await batch.commit();
      batch = db.batch();
      opCount = 0;
    }
  }

  if (opCount > 0) {
    await batch.commit();
  }

  console.log(`✓ Clean re-seeding complete! Total unique items: ${itemCounter}`);
}

cleanAndSeed().then(() => process.exit(0)).catch(err => {
  console.error(err);
  process.exit(1);
});
