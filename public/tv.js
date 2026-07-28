let currentBranchId = getQueryParam('branch') || 'branch_1';
let menuData = [];

function getQueryParam(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

document.addEventListener('DOMContentLoaded', async () => {
  await fetchTvMenu();
  // Live polling every 3 seconds for instant updates from Admin edits
  setInterval(fetchTvMenu, 3000);
});

async function fetchTvMenu() {
  try {
    const res = await fetch(`/api/branches/${currentBranchId}/menu?availableOnly=true`);
    const data = await res.json();

    const stringified = JSON.stringify(data);
    if (window._lastMenuData === stringified) return;
    window._lastMenuData = stringified;

    menuData = data;
    renderTvBoard();
  } catch (err) {
    console.error('Error fetching TV menu:', err);
  }
}

function renderTvBoard() {
  const board = document.getElementById('tv-board');
  if (!menuData || menuData.length === 0) {
    board.innerHTML = `
      <div style="grid-column: 1 / -1; display: flex; align-items: center; justify-content: center; height: 100%; color: var(--text-muted); font-size: 2rem; font-weight: 700;">
        No items available for this branch.
      </div>
    `;
    return;
  }

  // Group items by Category
  const categoryGroups = {};
  menuData.forEach(item => {
    const catName = item.categoryName || 'General';
    if (!categoryGroups[catName]) {
      categoryGroups[catName] = [];
    }
    categoryGroups[catName].push(item);
  });

  const categories = Object.keys(categoryGroups);
  const totalCatCount = categories.length;

  // Compute optimal Grid columns based on total categories & screen aspect ratio
  const isLandscape = window.innerWidth >= window.innerHeight;
  let landscapeCols = 5;
  let portraitCols = 3;

  if (totalCatCount <= 4) {
    landscapeCols = totalCatCount;
    portraitCols = 2;
  } else if (totalCatCount <= 10) {
    landscapeCols = 4;
    portraitCols = 2;
  } else if (totalCatCount <= 18) {
    landscapeCols = 5;
    portraitCols = 3;
  } else {
    landscapeCols = 6;
    portraitCols = 3;
  }

  board.style.setProperty('--cols-landscape', landscapeCols);
  board.style.setProperty('--cols-portrait', portraitCols);

  let boardHtml = '';

  categories.forEach(catName => {
    const items = categoryGroups[catName];
    items.sort((a, b) => (a.displayOrder || 999) - (b.displayOrder || 999));

    let itemsHtml = '';
    items.forEach(item => {
      itemsHtml += `
        <div class="item-row">
          <span class="item-title">${escapeHtml(item.name)}</span>
          <span class="item-dots"></span>
          <span class="item-price">₹${item.price}</span>
        </div>
      `;
    });

    boardHtml += `
      <div class="category-column">
        <div class="cat-header">
          <span class="cat-header-title">${escapeHtml(catName)}</span>
        </div>
        <div class="items-list">
          ${itemsHtml}
        </div>
      </div>
    `;
  });

  board.innerHTML = boardHtml;
}

window.addEventListener('resize', () => {
  if (menuData) renderTvBoard();
});

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
