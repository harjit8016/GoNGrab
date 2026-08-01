const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));
app.use(express.static(path.join(__dirname, 'web')));

// Initialize Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, 'config', 'grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json');
const localCachePath = path.join(__dirname, 'data', 'data_cache.json');

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

// 0. Get full live dataset (Branches, Categories, Items) directly from Firebase Firestore
app.get('/api/data', async (req, res) => {
  try {
    const cache = getLocalCache();
    res.json(cache);

    // Sync Firestore in background
    if (db) {
      Promise.all([
        db.collection('branches').get(),
        db.collection('categories').orderBy('displayOrder', 'asc').get(),
        db.collection('items').get()
      ]).then(([bSnap, cSnap, iSnap]) => {
        const branches = [];
        bSnap.forEach(doc => branches.push({ id: doc.id, ...doc.data() }));
        const categories = [];
        cSnap.forEach(doc => categories.push({ id: doc.id, ...doc.data() }));
        const items = [];
        iSnap.forEach(doc => items.push({ id: doc.id, ...doc.data() }));

        if (branches.length > 0 && items.length > 0) {
          const freshData = {
            branches,
            categories: categories.length > 0 ? categories : cache.categories,
            items,
            animatedSvgPack: cache.animatedSvgPack || []
          };
          fs.writeFileSync(localCachePath, JSON.stringify(freshData, null, 2));
        }
      }).catch(e => console.warn('Background Firestore sync notice:', e.message));
    }
  } catch (err) {
    const cache = getLocalCache();
    res.json(cache);
  }
});

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

// Save or Update Category directly to Firebase Firestore
app.post('/api/categories', async (req, res) => {
  try {
    const { id, name, displayOrder, animatedSvg } = req.body;
    if (!id || !name) return res.status(400).json({ error: 'Category ID and Name are required' });

    const docData = {
      id: id,
      name: name.trim(),
      displayOrder: parseInt(displayOrder) || 999,
      animatedSvg: animatedSvg || '',
      isActive: true,
      updatedAt: new Date()
    };

    if (db) {
      await db.collection('categories').doc(id).set(docData, { merge: true });
      console.log(`✓ Firebase Firestore categories/${id} updated with animatedSvg (${(animatedSvg || '').length} chars)`);
    }

    const cache = getLocalCache();
    const existingIdx = (cache.categories || []).findIndex(c => c.id === id);
    if (existingIdx >= 0) {
      cache.categories[existingIdx] = { ...cache.categories[existingIdx], ...docData };
    } else {
      if (!cache.categories) cache.categories = [];
      cache.categories.push(docData);
    }
    fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));

    res.status(200).json(docData);
  } catch (err) {
    console.error('Error saving category to Firestore:', err);
    res.status(500).json({ error: err.message });
  }
});

// Delete Category from Firebase Firestore
app.delete('/api/categories/:id', async (req, res) => {
  try {
    const { id } = req.params;
    if (db) {
      await db.collection('categories').doc(id).delete();
    }
    const cache = getLocalCache();
    cache.categories = (cache.categories || []).filter(c => c.id !== id);
    fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));
    res.status(200).json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
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
  const sanitizeKey = (str) => str ? String(str).toLowerCase().replace(/[^a-z0-9]/g, '') : '';
  const categoriesMap = {};

  (cache.categories || []).forEach(c => {
    if (c.id) {
      categoriesMap[c.id] = c;
      categoriesMap[sanitizeKey(c.id)] = c;
    }
    if (c.name) {
      categoriesMap[sanitizeKey(c.name)] = c;
    }
  });

  let branchItems = cache.items.map(item => {
    const bData = (item.branches && item.branches[branchId]) || { available: true, price: item.defaultPrice };
    const catInfo = categoriesMap[item.categoryId] || 
                    categoriesMap[sanitizeKey(item.categoryId)] || 
                    categoriesMap[sanitizeKey(item.categoryName)] || {};

    return {
      id: item.id,
      itemId: item.id,
      name: item.name,
      categoryId: item.categoryId,
      categoryName: item.categoryName,
      price: bData.price !== undefined ? bData.price : item.defaultPrice,
      isAvailable: bData.available !== undefined ? bData.available : true,
      displayOrder: item.displayOrder,
      animatedSvg: item.animatedSvg || catInfo.animatedSvg || '',
      iconKey: item.iconKey || catInfo.iconKey || ''
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
    const body = req.body || {};
    const { name, categoryId, categoryName, defaultPrice, branches, displayOrder } = body;

    const cache = getLocalCache();
    let item = cache.items.find(i => i.id === itemId);

    if (item) {
      if (name) item.name = name.trim();
      if (categoryId) item.categoryId = categoryId;
      if (categoryName) item.categoryName = categoryName;
      if (defaultPrice !== undefined) item.defaultPrice = parseFloat(defaultPrice);
      if (branches) item.branches = branches;
      if (displayOrder !== undefined) item.displayOrder = displayOrder;
    } else {
      item = { id: itemId, itemId, ...body };
      cache.items.push(item);
    }
    fs.writeFileSync(localCachePath, JSON.stringify(cache, null, 2));

    if (db) {
      try {
        const itemRef = db.collection('items').doc(itemId);
        const docUpdates = { updatedAt: new Date() };
        if (name) docUpdates.name = name.trim();
        if (categoryId) docUpdates.categoryId = categoryId;
        if (categoryName) docUpdates.categoryName = categoryName;
        if (defaultPrice !== undefined) docUpdates.defaultPrice = parseFloat(defaultPrice);
        if (branches) docUpdates.branches = branches;
        if (displayOrder !== undefined) docUpdates.displayOrder = displayOrder;

        await itemRef.set(docUpdates, { merge: true });
        console.log(`✓ Firebase Firestore items/${itemId} updated (${name || item.name})`);
      } catch (e) {
        console.warn('Firestore edit error:', e.message);
      }
    }

    res.json(item);
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
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Resilient Restaurant Menu Server running on http://0.0.0.0:${PORT}`);
});
