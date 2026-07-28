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
const localCachePath = path.join(__dirname, 'data_cache.json');

let db = null;
if (fs.existsSync(serviceAccountPath)) {
  try {
    const serviceAccount = require(serviceAccountPath);
    initializeApp({ credential: cert(serviceAccount) });
    db = getFirestore();
  } catch (e) {
    console.error('Firebase init error:', e.message);
  }
}

function getLocalCache() {
  if (fs.existsSync(localCachePath)) {
    return JSON.parse(fs.readFileSync(localCachePath, 'utf8'));
  }
  return { branches: [], categories: [], items: [] };
}

function slugify(text) {
  return text.toString().toLowerCase()
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^\w\-]+/g, '')
    .replace(/\-\-+/g, '_');
}

// --------------------------------------------------------------------------
// API ENDPOINTS (Firestore + Resilient Offline Local Fallback)
// --------------------------------------------------------------------------

// 1. Get all branches
app.get('/api/branches', async (req, res) => {
  try {
    if (db) {
      const snapshot = await db.collection('branches').get();
      const branches = [];
      snapshot.forEach(doc => branches.push({ id: doc.id, ...doc.data() }));
      if (branches.length > 0) return res.json(branches);
    }
  } catch (err) {
    console.warn('Firestore fallback triggered for /api/branches:', err.message);
  }
  const cache = getLocalCache();
  res.json(cache.branches);
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

    if (db) {
      await db.collection('branches').doc(branchId).set(branchDoc, { merge: true });
    }

    // Save to local cache
    const cache = getLocalCache();
    if (!cache.branches.find(b => b.id === branchId)) {
      cache.branches.push(branchDoc);
      fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));
    }

    res.status(201).json(branchDoc);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 3. Get categories
app.get('/api/categories', async (req, res) => {
  try {
    if (db) {
      const snapshot = await db.collection('categories').orderBy('displayOrder', 'asc').get();
      const categories = [];
      snapshot.forEach(doc => categories.push({ id: doc.id, ...doc.data() }));
      if (categories.length > 0) return res.json(categories);
    }
  } catch (err) {
    console.warn('Firestore fallback triggered for /api/categories:', err.message);
  }
  const cache = getLocalCache();
  res.json(cache.categories);
});

// 4. Get items
app.get('/api/items', async (req, res) => {
  try {
    if (db) {
      const snapshot = await db.collection('items').get();
      const masterItems = [];
      snapshot.forEach(doc => masterItems.push({ id: doc.id, ...doc.data() }));
      if (masterItems.length > 0) return res.json(masterItems);
    }
  } catch (err) {
    console.warn('Firestore fallback triggered for /api/items:', err.message);
  }
  const cache = getLocalCache();
  res.json(cache.items);
});

// 5. Get branch specific menu (Optimized TV & POS endpoint with Resilient Cache Fallback)
app.get('/api/branches/:branchId/menu', async (req, res) => {
  const { branchId } = req.params;
  const { availableOnly } = req.query;

  try {
    if (db) {
      let subQuery = db.collection('branches').doc(branchId).collection('menu_items');
      if (availableOnly === 'true') {
        subQuery = subQuery.where('isAvailable', '==', true);
      }
      const snapshot = await subQuery.get();
      const menuItems = [];
      snapshot.forEach(doc => menuItems.push({ id: doc.id, ...doc.data() }));
      if (menuItems.length > 0) return res.json(menuItems);
    }
  } catch (err) {
    console.warn(`Firestore fallback triggered for /api/branches/${branchId}/menu:`, err.message);
  }

  // Resilient Local Cache Fallback
  const cache = getLocalCache();
  let branchItems = cache.items.map(item => {
    const bData = (item.branches && item.branches[branchId]) || { available: true, price: item.defaultPrice };
    return {
      id: item.id,
      itemId: item.id,
      name: item.name,
      categoryId: item.categoryId,
      categoryName: item.categoryName,
      price: bData.price !== undefined ? bData.price : item.defaultPrice,
      isAvailable: bData.available !== undefined ? bData.available : true,
      displayOrder: item.displayOrder
    };
  });

  if (availableOnly === 'true') {
    branchItems = branchItems.filter(i => i.isAvailable);
  }

  res.json(branchItems);
});

// 6. Add new item
app.post('/api/items', async (req, res) => {
  try {
    const { name, categoryId, categoryName, defaultPrice, branchSelections } = req.body;
    if (!name || !categoryId || defaultPrice === undefined) {
      return res.status(400).json({ error: 'Name, Category, and Default Price are required' });
    }

    const itemId = `${categoryId}_${slugify(name)}`;
    const priceVal = parseFloat(defaultPrice);

    const itemDoc = {
      id: itemId,
      itemId: itemId,
      name: name.trim(),
      categoryId: categoryId,
      categoryName: categoryName || categoryId,
      defaultPrice: priceVal,
      displayOrder: 999,
      branches: branchSelections || {
        branch_1: { available: true, price: priceVal, isAvailable: true },
        branch_2: { available: true, price: priceVal, isAvailable: true }
      }
    };

    if (db) {
      try {
        await db.collection('items').doc(itemId).set(itemDoc, { merge: true });
      } catch (e) {
        console.warn('Firestore write error:', e.message);
      }
    }

    // Save to local cache
    const cache = getLocalCache();
    const existingIdx = cache.items.findIndex(i => i.id === itemId);
    if (existingIdx >= 0) {
      cache.items[existingIdx] = itemDoc;
    } else {
      cache.items.push(itemDoc);
    }
    fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));

    res.status(201).json(itemDoc);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 7. Edit item
app.put('/api/items/:id', async (req, res) => {
  try {
    const itemId = req.params.id;
    const { name, categoryId, categoryName, defaultPrice, branches } = req.body;

    const cache = getLocalCache();
    const item = cache.items.find(i => i.id === itemId);
    if (item) {
      if (name) item.name = name.trim();
      if (categoryId) item.categoryId = categoryId;
      if (categoryName) item.categoryName = categoryName;
      if (defaultPrice !== undefined) item.defaultPrice = parseFloat(defaultPrice);
      if (branches) item.branches = branches;

      fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));
    }

    if (db) {
      try {
        const itemRef = db.collection('items').doc(itemId);
        await itemRef.set({ name, categoryId, categoryName, defaultPrice, branches }, { merge: true });
      } catch (e) {
        console.warn('Firestore edit error:', e.message);
      }
    }

    res.json(item || { id: itemId, name, categoryId, categoryName, defaultPrice });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 8. Patch branch status
app.patch('/api/items/:id/branch-status', async (req, res) => {
  try {
    const itemId = req.params.id;
    const { branchId, available, price } = req.body;

    const cache = getLocalCache();
    const item = cache.items.find(i => i.id === itemId);
    if (item) {
      if (!item.branches) item.branches = {};
      if (!item.branches[branchId]) item.branches[branchId] = {};

      if (available !== undefined) item.branches[branchId].available = Boolean(available);
      if (price !== undefined) item.branches[branchId].price = parseFloat(price);

      fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));
    }

    if (db) {
      try {
        const subRef = db.collection('branches').doc(branchId).collection('menu_items').doc(itemId);
        const updates = {};
        if (available !== undefined) updates.isAvailable = Boolean(available);
        if (price !== undefined) updates.price = parseFloat(price);
        await subRef.set(updates, { merge: true });
      } catch (e) {
        console.warn('Firestore patch error:', e.message);
      }
    }

    res.json({ success: true, itemId, branchId, available, price });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 9. Delete item
app.delete('/api/items/:id', async (req, res) => {
  try {
    const itemId = req.params.id;
    const cache = getLocalCache();
    cache.items = cache.items.filter(i => i.id !== itemId);
    fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));

    if (db) {
      try {
        await db.collection('items').doc(itemId).delete();
      } catch (e) {
        console.warn('Firestore delete error:', e.message);
      }
    }

    res.json({ success: true, message: 'Item deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 Resilient Restaurant Menu Server running on http://localhost:${PORT}`);
});
