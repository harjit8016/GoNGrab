const xlsx = require('xlsx');
const fs = require('fs');
const path = require('path');

function slugify(text) {
  return text.toString().toLowerCase()
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^\w\-]+/g, '')
    .replace(/\-\-+/g, '_');
}

function generateLocalCache() {
  const excelPath = '/tmp/menu_sheet.xlsx';
  if (!fs.existsSync(excelPath)) {
    console.error('Excel file /tmp/menu_sheet.xlsx not found!');
    return;
  }

  const workbook = xlsx.readFile(excelPath);

  // Categories
  const categoriesSheet = workbook.Sheets['Categories'];
  const categoriesData = xlsx.utils.sheet_to_json(categoriesSheet, { header: 1 });
  const categories = [];
  let categoryOrder = 1;

  for (let i = 1; i < categoriesData.length; i++) {
    const row = categoriesData[i];
    if (row && row[0]) {
      const catName = row[0].toString().trim();
      const catId = slugify(catName);
      categories.push({
        id: catId,
        name: catName,
        displayOrder: categoryOrder++,
        isActive: true
      });
    }
  }

  // Branches
  const branches = [
    { id: 'branch_1', name: 'Branch 1', code: 'BR1', isActive: true },
    { id: 'branch_2', name: 'Branch 2', code: 'BR2', isActive: true }
  ];

  // Items
  const masterSheet = workbook.Sheets['Items (Master)'];
  const masterRows = xlsx.utils.sheet_to_json(masterSheet, { header: 1 });

  let currentCatName = 'General';
  let itemOrder = 1;
  const items = [];
  const seenNames = new Set();

  for (let i = 1; i < masterRows.length; i++) {
    const row = masterRows[i];
    if (!row || row.length === 0) continue;

    let [cat, itemName, defaultPrice, b1Price, b2Price, b1Avail, b2Avail] = row;

    if (cat && cat.toString().trim()) {
      currentCatName = cat.toString().trim();
    }

    if (!itemName || !itemName.toString().trim()) continue;

    const name = itemName.toString().trim();
    const normName = name.toLowerCase();
    if (seenNames.has(normName)) continue;
    seenNames.add(normName);

    const catId = slugify(currentCatName);
    const rawId = slugify(name);
    const itemId = `${catId}_${rawId}`;

    const price = parseFloat(defaultPrice) || 0;
    const branch1Price = parseFloat(b1Price) || price;
    const branch2Price = parseFloat(b2Price) || price;
    const branch1Available = (b1Avail === true || b1Avail === 'TRUE' || b1Avail === 'true' || b1Avail === 1);
    const branch2Available = (b2Avail === true || b2Avail === 'TRUE' || b2Avail === 'true' || b2Avail === 1);

    items.push({
      id: itemId,
      itemId: itemId,
      name: name,
      categoryId: catId,
      categoryName: currentCatName,
      defaultPrice: price,
      displayOrder: itemOrder++,
      branches: {
        branch_1: { available: branch1Available, price: branch1Price, isAvailable: branch1Available },
        branch_2: { available: branch2Available, price: branch2Price, isAvailable: branch2Available }
      }
    });
  }

  const cacheData = { branches, categories, items };
  const cachePath = path.join(__dirname, '../data/data_cache.json');
  fs.writeFileSync(cachePath, JSON.stringify(cacheData, null, 2));
  console.log(`✓ Local menu data cache generated cleanly at ${cachePath} with ${items.length} items.`);
}

generateLocalCache();
