let state = {
  branches: [],
  categories: [],
  items: [],
  activeBranchId: 'all',
  activeCategoryId: 'all',
  searchQuery: ''
};

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
  await loadData();
});

async function loadData() {
  try {
    const [branchesRes, categoriesRes, itemsRes] = await Promise.all([
      fetch('/api/branches'),
      fetch('/api/categories'),
      fetch('/api/items')
    ]);

    state.branches = await branchesRes.json();
    state.categories = await categoriesRes.json();
    state.items = await itemsRes.json();

    renderStats();
    renderBranchTabs();
    renderCategories();
    renderTableHeader();
    renderItemsTable();
    populateCategoryDropdown();
  } catch (err) {
    console.error('Failed to load menu data:', err);
  }
}

function renderStats() {
  document.getElementById('stat-total-items').textContent = state.items.length;
  document.getElementById('stat-total-categories').textContent = state.categories.length;
  document.getElementById('stat-total-branches').textContent = state.branches.length;

  const filtered = getFilteredItems();
  document.getElementById('stat-active-view-items').textContent = filtered.length;
}

function renderBranchTabs() {
  const container = document.getElementById('branch-tabs-container');
  let html = `
    <button class="branch-tab ${state.activeBranchId === 'all' ? 'active' : ''}" onclick="selectBranch('all')">
      All Branches
    </button>
  `;

  state.branches.forEach(branch => {
    html += `
      <button class="branch-tab ${state.activeBranchId === branch.id ? 'active' : ''}" onclick="selectBranch('${branch.id}')">
        ${branch.name}
      </button>
    `;
  });

  container.innerHTML = html;
}

function selectBranch(branchId) {
  state.activeBranchId = branchId;
  renderBranchTabs();
  renderTableHeader();
  renderItemsTable();
  renderStats();
}

function renderCategories() {
  const container = document.getElementById('categories-container');
  let html = `
    <div class="category-pill ${state.activeCategoryId === 'all' ? 'active' : ''}" onclick="selectCategory('all')">
      All Categories (${state.items.length})
    </div>
  `;

  state.categories.forEach(cat => {
    const count = state.items.filter(i => i.categoryId === cat.id).length;
    html += `
      <div class="category-pill ${state.activeCategoryId === cat.id ? 'active' : ''}" onclick="selectCategory('${cat.id}')">
        ${cat.name} (${count})
      </div>
    `;
  });

  container.innerHTML = html;
}

function selectCategory(catId) {
  state.activeCategoryId = catId;
  renderCategories();
  renderItemsTable();
  renderStats();
}

function filterItems() {
  state.searchQuery = document.getElementById('search-input').value.toLowerCase().trim();
  renderItemsTable();
  renderStats();
}

function getFilteredItems() {
  return state.items.filter(item => {
    // Category match
    if (state.activeCategoryId !== 'all' && item.categoryId !== state.activeCategoryId) {
      return false;
    }

    // Branch match (if specific branch selected)
    if (state.activeBranchId !== 'all') {
      const branchData = item.branches && item.branches[state.activeBranchId];
      if (!branchData) return false;
    }

    // Search query match
    if (state.searchQuery) {
      const matchName = item.name.toLowerCase().includes(state.searchQuery);
      const matchCat = (item.categoryName || '').toLowerCase().includes(state.searchQuery);
      return matchName || matchCat;
    }

    return true;
  });
}

function renderTableHeader() {
  const headerRow = document.getElementById('table-header-row');
  let branchColsHtml = '';

  if (state.activeBranchId === 'all') {
    state.branches.forEach(branch => {
      branchColsHtml += `<th>${branch.name} (Price & Availability)</th>`;
    });
  } else {
    const activeBranch = state.branches.find(b => b.id === state.activeBranchId);
    branchColsHtml += `<th>${activeBranch ? activeBranch.name : 'Branch'} (Price & Availability)</th>`;
  }

  headerRow.innerHTML = `
    <th>Item Name</th>
    <th>Category</th>
    <th>Default Price</th>
    ${branchColsHtml}
    <th style="text-align: right;">Actions</th>
  `;
}

function renderItemsTable() {
  const tbody = document.getElementById('menu-items-tbody');
  const filtered = getFilteredItems();

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="10" style="text-align: center; color: var(--text-muted); padding: 40px;">
          No menu items found.
        </td>
      </tr>
    `;
    return;
  }

  let html = '';
  filtered.forEach(item => {
    let branchCols = '';

    const branchesToRender = state.activeBranchId === 'all' 
      ? state.branches 
      : state.branches.filter(b => b.id === state.activeBranchId);

    branchesToRender.forEach(branch => {
      const bData = (item.branches && item.branches[branch.id]) || { available: false, price: item.defaultPrice };
      const isAvailable = bData.available;
      const currentPrice = bData.price !== undefined ? bData.price : item.defaultPrice;

      branchCols += `
        <td>
          <div class="branch-cell">
            <span class="status-badge ${isAvailable ? 'on' : 'off'}">${isAvailable ? 'ON' : 'OFF'}</span>
            <label class="switch">
              <input type="checkbox" ${isAvailable ? 'checked' : ''} onchange="toggleBranchAvailability('${item.id}', '${branch.id}', this.checked)">
              <span class="slider"></span>
            </label>
            <div style="display: flex; align-items: center; gap: 4px;">
              <span style="color: var(--text-muted); font-size: 0.85rem;">₹</span>
              <input type="number" class="price-input" value="${currentPrice}" onchange="updateBranchPrice('${item.id}', '${branch.id}', this.value)">
            </div>
          </div>
        </td>
      `;
    });

    html += `
      <tr>
        <td class="item-name">${escapeHtml(item.name)}</td>
        <td><span class="category-tag">${escapeHtml(item.categoryName || 'General')}</span></td>
        <td style="font-weight: 600;">₹${item.defaultPrice}</td>
        ${branchCols}
        <td style="text-align: right;">
          <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 0.85rem;" onclick="openEditItemModal('${item.id}')">
            <i class="fa-solid fa-pen"></i> Edit
          </button>
          <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 0.85rem; color: var(--accent-red);" onclick="deleteItem('${item.id}')">
            <i class="fa-solid fa-trash"></i>
          </button>
        </td>
      </tr>
    `;
  });

  tbody.innerHTML = html;
}

// Quick Inline API Actions
async function toggleBranchAvailability(itemId, branchId, isAvailable) {
  try {
    await fetch(`/api/items/${itemId}/branch-status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ branchId, available: isAvailable })
    });

    // Update local state
    const item = state.items.find(i => i.id === itemId);
    if (item) {
      if (!item.branches) item.branches = {};
      if (!item.branches[branchId]) item.branches[branchId] = {};
      item.branches[branchId].available = isAvailable;
    }
    renderItemsTable();
  } catch (err) {
    console.error('Error updating status:', err);
  }
}

async function updateBranchPrice(itemId, branchId, newPrice) {
  const priceVal = parseFloat(newPrice);
  if (isNaN(priceVal)) return;

  try {
    await fetch(`/api/items/${itemId}/branch-status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ branchId, price: priceVal })
    });

    const item = state.items.find(i => i.id === itemId);
    if (item) {
      if (!item.branches) item.branches = {};
      if (!item.branches[branchId]) item.branches[branchId] = {};
      item.branches[branchId].price = priceVal;
    }
    renderItemsTable();
  } catch (err) {
    console.error('Error updating price:', err);
  }
}

// Populate Modal Categories Select
function populateCategoryDropdown() {
  const select = document.getElementById('form-item-category');
  select.innerHTML = state.categories.map(c => `
    <option value="${c.id}">${c.name}</option>
  `).join('');
}

// Modal Handlers
function openAddItemModal() {
  document.getElementById('modal-title').textContent = 'Add New Item';
  document.getElementById('edit-item-id').value = '';
  document.getElementById('form-item-name').value = '';
  document.getElementById('form-item-price').value = '';
  
  if (state.categories.length > 0) {
    document.getElementById('form-item-category').value = state.categories[0].id;
  }

  renderModalBranchConfigs();
  document.getElementById('item-modal').classList.add('active');
}

function openEditItemModal(itemId) {
  const item = state.items.find(i => i.id === itemId);
  if (!item) return;

  document.getElementById('modal-title').textContent = 'Edit Item';
  document.getElementById('edit-item-id').value = item.id;
  document.getElementById('form-item-name').value = item.name;
  document.getElementById('form-item-price').value = item.defaultPrice;
  document.getElementById('form-item-category').value = item.categoryId;

  renderModalBranchConfigs(item.branches, item.defaultPrice);
  document.getElementById('item-modal').classList.add('active');
}

function renderModalBranchConfigs(itemBranches = {}, defaultPrice = 0) {
  const container = document.getElementById('modal-branch-configs');
  let html = '';

  state.branches.forEach(branch => {
    const bData = itemBranches[branch.id] || { available: true, price: defaultPrice };
    html += `
      <div class="branch-config-item">
        <div>
          <strong>${branch.name}</strong>
        </div>
        <div style="display: flex; align-items: center; gap: 12px;">
          <label class="switch">
            <input type="checkbox" id="modal-branch-avail-${branch.id}" ${bData.available ? 'checked' : ''}>
            <span class="slider"></span>
          </label>
          <div style="display: flex; align-items: center; gap: 4px;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">₹</span>
            <input type="number" step="0.01" class="price-input" id="modal-branch-price-${branch.id}" value="${bData.price !== undefined ? bData.price : defaultPrice}">
          </div>
        </div>
      </div>
    `;
  });

  container.innerHTML = html;
}

function closeItemModal() {
  document.getElementById('item-modal').classList.remove('active');
}

function openAddBranchModal() {
  document.getElementById('form-branch-name').value = '';
  document.getElementById('form-branch-code').value = '';
  document.getElementById('branch-modal').classList.add('active');
}

function closeBranchModal() {
  document.getElementById('branch-modal').classList.remove('active');
}

// Form Submissions
async function saveItem(event) {
  event.preventDefault();
  const itemId = document.getElementById('edit-item-id').value;
  const name = document.getElementById('form-item-name').value.trim();
  const categoryId = document.getElementById('form-item-category').value;
  const catObj = state.categories.find(c => c.id === categoryId);
  const categoryName = catObj ? catObj.name : categoryId;
  const defaultPrice = parseFloat(document.getElementById('form-item-price').value);

  const branchesMap = {};
  state.branches.forEach(b => {
    const availCheckbox = document.getElementById(`modal-branch-avail-${b.id}`);
    const priceInput = document.getElementById(`modal-branch-price-${b.id}`);
    branchesMap[b.id] = {
      available: availCheckbox ? availCheckbox.checked : false,
      price: priceInput ? parseFloat(priceInput.value) : defaultPrice
    };
  });

  const payload = {
    name,
    categoryId,
    categoryName,
    defaultPrice,
    branchSelections: branchesMap,
    branches: branchesMap
  };

  try {
    if (itemId) {
      // Edit
      await fetch(`/api/items/${itemId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    } else {
      // Create
      await fetch('/api/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }

    closeItemModal();
    await loadData();
  } catch (err) {
    console.error('Error saving item:', err);
  }
}

async function saveBranch(event) {
  event.preventDefault();
  const name = document.getElementById('form-branch-name').value.trim();
  const code = document.getElementById('form-branch-code').value.trim();

  try {
    await fetch('/api/branches', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, code })
    });

    closeBranchModal();
    await loadData();
  } catch (err) {
    console.error('Error creating branch:', err);
  }
}

async function deleteItem(itemId) {
  if (!confirm('Are you sure you want to delete this menu item?')) return;

  try {
    await fetch(`/api/items/${itemId}`, { method: 'DELETE' });
    await loadData();
  } catch (err) {
    console.error('Error deleting item:', err);
  }
}

function escapeHtml(text) {
  if (!text) return '';
  return text.replace(/[&<>"']/g, function(m) {
    return {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    }[m];
  });
}
