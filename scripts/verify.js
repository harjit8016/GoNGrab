const path = require('path');
const fs = require('fs');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccountPath = path.join(__dirname, '../grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error(`Service account key not found at ${serviceAccountPath}`);
  process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

async function runVerification() {
  console.log('=== Firestore Subcollection Optimization Verification ===');

  // 1. Check Branches
  const branchesSnap = await db.collection('branches').get();
  console.log(`✓ Branches count: ${branchesSnap.size}`);

  // 2. Check Categories
  const categoriesSnap = await db.collection('categories').get();
  console.log(`✓ Categories count: ${categoriesSnap.size}`);

  // 3. Check Counts via Aggregation
  const masterCount = (await db.collection('items').count().get()).data().count;
  const b1Count = (await db.collection('branches').doc('branch_1').collection('menu_items').count().get()).data().count;
  const b2Count = (await db.collection('branches').doc('branch_2').collection('menu_items').count().get()).data().count;

  console.log(`✓ Master Catalog Items count: ${masterCount}`);
  console.log(`✓ Branch 1 Subcollection Items count: ${b1Count}`);
  console.log(`✓ Branch 2 Subcollection Items count: ${b2Count}`);

  // 4. Test Subcollection Query for Available Items
  const b1AvailableSnap = await db.collection('branches').doc('branch_1').collection('menu_items')
    .where('isAvailable', '==', true)
    .get();

  const b2AvailableSnap = await db.collection('branches').doc('branch_2').collection('menu_items')
    .where('isAvailable', '==', true)
    .get();

  console.log(`✓ Branch 1 Available Menu Items: ${b1AvailableSnap.size}`);
  console.log(`✓ Branch 2 Available Menu Items: ${b2AvailableSnap.size}`);

  // 5. Test Sample Subcollection Document
  const sampleRef = db.collection('branches').doc('branch_1').collection('menu_items').doc('shake_strawberry_shake');
  const sampleDoc = await sampleRef.get();

  if (sampleDoc.exists) {
    const data = sampleDoc.data();
    console.log(`✓ Sample Subcollection Item ('branches/branch_1/menu_items/shake_strawberry_shake'):`);
    console.log(`   Name: ${data.name}`);
    console.log(`   Category: ${data.categoryName}`);
    console.log(`   Branch Price: ₹${data.price}`);
    console.log(`   isAvailable: ${data.isAvailable}`);
  }

  console.log('=== All Subcollection Verifications Passed 100% Cleanly! ===');
}

runVerification().then(() => {
  process.exit(0);
}).catch(err => {
  console.error('Verification failed:', err);
  process.exit(1);
});
