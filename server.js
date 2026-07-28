const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Initialize Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, 'grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json');

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

// --------------------------------------------------------------------------
// API ENDPOINTS (Optimized Subcollection Architecture)
// --------------------------------------------------------------------------

// 1. Get all branches
app.get('/api/branches', async (req, res) => {
  try {
    const snapshot = await db.collection('branches').get();
    const branches = [];
    snapshot.forEach(doc => {
      branches.push({ id: doc.id, ...doc.data() });
    });
    res.json(branches);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 2. Create new branch
app.post('/api/branches', async (req, res) => {
  try {
    const { name, code } = req.body;
    if (!name) return res.status(400).json({ error: 'Branch name is required' });

    const branchId = slugify(name);
    const branchDoc = {
      id: branchId,
      name: name.trim(),
      code: code ? code.trim().toUpperCase() : branchId.toUpperCase().slice(0, 4),
      isActive: true,
      createdAt: new Date()
    };

    await db.collection('branches').doc(branchId).set(branchDoc, { merge: true });

    // Initialize subcollection for this new branch using Master Items Catalog
    const itemsSnapshot = await db.collection('items').get();
    const batch = db.batch();

    itemsSnapshot.forEach(doc => {
      const itemData = doc.data();
      const subDocRef = db.collection('branches').doc(branchId).collection('menu_items').doc(doc.id);
      batch.set(subDocRef, {
        itemId: doc.id,
        name: itemData.name,
        categoryId: itemData.categoryId,
        categoryName: itemData.categoryName,
        price: itemData.defaultPrice || 0,
        isAvailable: false,
        displayOrder: itemData.displayOrder || 999,
        updatedAt: new Date()
      }, { merge: true });
    });

    await batch.commit();
    res.status(201).json(branchDoc);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 3. Get categories
app.get('/api/categories', async (req, res) => {
  try {
    const snapshot = await db.collection('categories').orderBy('displayOrder', 'asc').get();
    const categories = [];
    snapshot.forEach(doc => {
      categories.push({ id: doc.id, ...doc.data() });
    });
    res.json(categories);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 4. Get items (Master Catalog merged with Branch Subcollections)
app.get('/api/items', async (req, res) => {
  try {
    const { categoryId, branchId } = req.query;
    let query = db.collection('items');

    if (categoryId) {
      query = query.where('categoryId', '==', categoryId);
    }

    const masterSnapshot = await query.get();
    const masterItems = [];
    masterSnapshot.forEach(doc => {
      masterItems.push({ id: doc.id, ...doc.data() });
    });

    // Fetch all branch subcollections to build UI view
    const branchesSnapshot = await db.collection('branches').get();
    const branchIds = [];
    branchesSnapshot.forEach(bDoc => branchIds.push(bDoc.id));

    // Map branch menu item states into each item
    for (const item of masterItems) {
      item.branches = {};
      for (const bId of branchIds) {
        const subDoc = await db.collection('branches').doc(bId).collection('menu_items').doc(item.id).get();
        if (subDoc.exists) {
          const subData = subDoc.data();
          item.branches[bId] = {
            available: subData.isAvailable,
            price: subData.price
          };
        } else {
          item.branches[bId] = {
            available: false,
            price: item.defaultPrice
          };
        }
      }
    }

    // Filter by branch availability if branchId specified
    let filteredItems = masterItems;
    if (branchId) {
      filteredItems = masterItems.filter(item => item.branches[branchId] && item.branches[branchId].available);
    }

    filteredItems.sort((a, b) => (a.displayOrder || 999) - (b.displayOrder || 999));
    res.json(filteredItems);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 5. Get branch specific menu (Optimized endpoint for TV displays & POS)
app.get('/api/branches/:branchId/menu', async (req, res) => {
  try {
    const { branchId } = req.params;
    const { availableOnly } = req.query;

    let subQuery = db.collection('branches').doc(branchId).collection('menu_items');
    if (availableOnly === 'true') {
      subQuery = subQuery.where('isAvailable', '==', true);
    }

    const snapshot = await subQuery.get();
    const menuItems = [];
    snapshot.forEach(doc => {
      menuItems.push({ id: doc.id, ...doc.data() });
    });

    res.json(menuItems);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 6. Add new item (Creates Master Item + Branch Subcollection entries)
app.post('/api/items', async (req, res) => {
  try {
    const { name, categoryId, categoryName, defaultPrice, branchSelections } = req.body;
    if (!name || !categoryId || defaultPrice === undefined) {
      return res.status(400).json({ error: 'Name, Category, and Default Price are required' });
    }

    const itemId = slugify(name);
    const masterItemDoc = {
      id: itemId,
      name: name.trim(),
      categoryId: categoryId,
      categoryName: categoryName || categoryId,
      defaultPrice: parseFloat(defaultPrice),
      displayOrder: 999,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    // Save Master Item
    await db.collection('items').doc(itemId).set(masterItemDoc, { merge: true });

    // Save Subcollection documents for all branches
    const branchesSnapshot = await db.collection('branches').get();
    const batch = db.batch();

    branchesSnapshot.forEach(bDoc => {
      const bId = bDoc.id;
      const config = branchSelections && branchSelections[bId] ? branchSelections[bId] : null;
      const isAvailable = config ? Boolean(config.available) : false;
      const price = config && config.price !== undefined ? parseFloat(config.price) : parseFloat(defaultPrice);

      const subRef = db.collection('branches').doc(bId).collection('menu_items').doc(itemId);
      batch.set(subRef, {
        itemId: itemId,
        name: name.trim(),
        categoryId: categoryId,
        categoryName: categoryName || categoryId,
        price: price,
        isAvailable: isAvailable,
        displayOrder: 999,
        updatedAt: new Date()
      }, { merge: true });
    });

    await batch.commit();
    res.status(201).json(masterItemDoc);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 7. Edit existing item (Updates Master Item & syncs across Branch Subcollections)
app.put('/api/items/:id', async (req, res) => {
  try {
    const itemId = req.params.id;
    const { name, categoryId, categoryName, defaultPrice, branches } = req.body;

    const itemRef = db.collection('items').doc(itemId);
    const doc = await itemRef.get();
    if (!doc.exists) {
      return res.status(404).json({ error: 'Item not found' });
    }

    const masterUpdates = { updatedAt: new Date() };
    if (name) masterUpdates.name = name.trim();
    if (categoryId) masterUpdates.categoryId = categoryId;
    if (categoryName) masterUpdates.categoryName = categoryName;
    if (defaultPrice !== undefined) masterUpdates.defaultPrice = parseFloat(defaultPrice);

    await itemRef.update(masterUpdates);

    // Sync subcollections
    const branchesSnapshot = await db.collection('branches').get();
    const batch = db.batch();

    branchesSnapshot.forEach(bDoc => {
      const bId = bDoc.id;
      const bConfig = branches && branches[bId] ? branches[bId] : null;

      const subRef = db.collection('branches').doc(bId).collection('menu_items').doc(itemId);
      const subUpdates = { updatedAt: new Date() };

      if (name) subUpdates.name = name.trim();
      if (categoryId) subUpdates.categoryId = categoryId;
      if (categoryName) subUpdates.categoryName = categoryName;

      if (bConfig) {
        if (bConfig.available !== undefined) subUpdates.isAvailable = Boolean(bConfig.available);
        if (bConfig.price !== undefined) subUpdates.price = parseFloat(bConfig.price);
      }

      batch.set(subRef, subUpdates, { merge: true });
    });

    await batch.commit();
    const updatedDoc = await itemRef.get();
    res.json({ id: updatedDoc.id, ...updatedDoc.data() });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 8. Quick branch status & price toggle endpoint (Targets Branch Subcollection directly)
app.patch('/api/items/:id/branch-status', async (req, res) => {
  try {
    const itemId = req.params.id;
    const { branchId, available, price } = req.body;

    if (!branchId) {
      return res.status(400).json({ error: 'branchId is required' });
    }

    const subRef = db.collection('branches').doc(branchId).collection('menu_items').doc(itemId);
    const updates = { updatedAt: new Date() };

    if (available !== undefined) {
      updates.isAvailable = Boolean(available);
    }
    if (price !== undefined) {
      updates.price = parseFloat(price);
    }

    await subRef.set(updates, { merge: true });
    res.json({ success: true, itemId, branchId, available, price });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 9. Delete item from master & all branch subcollections
app.delete('/api/items/:id', async (req, res) => {
  try {
    const itemId = req.params.id;
    await db.collection('items').doc(itemId).delete();

    const branchesSnapshot = await db.collection('branches').get();
    const batch = db.batch();

    branchesSnapshot.forEach(bDoc => {
      const subRef = db.collection('branches').doc(bDoc.id).collection('menu_items').doc(itemId);
      batch.delete(subRef);
    });

    await batch.commit();
    res.json({ success: true, message: 'Item deleted from master & branch subcollections' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 Optimized Restaurant Menu Server running on http://localhost:${PORT}`);
});
