let currentBranchId = getQueryParam('branch') || 'branch_1';
let menuData = [];

function getQueryParam(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

document.addEventListener('DOMContentLoaded', async () => {
  await fetchTvMenu();
  setInterval(fetchTvMenu, 3000);
});

// Fullscreen API Handlers
function toggleFullscreen() {
  if (!document.fullscreenElement && !document.webkitFullscreenElement) {
    const docEl = document.documentElement;
    if (docEl.requestFullscreen) {
      docEl.requestFullscreen().catch(err => console.warn('Fullscreen error:', err));
    } else if (docEl.webkitRequestFullscreen) {
      docEl.webkitRequestFullscreen();
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    }
  }
}

document.addEventListener('fullscreenchange', handleFullscreenChange);
document.addEventListener('webkitfullscreenchange', handleFullscreenChange);

function handleFullscreenChange() {
  const expandIcon = document.getElementById('fs-icon-expand');
  const compressIcon = document.getElementById('fs-icon-compress');
  const isFs = document.fullscreenElement || document.webkitFullscreenElement;

  if (isFs) {
    if (expandIcon) expandIcon.style.display = 'none';
    if (compressIcon) compressIcon.style.display = 'inline-block';
  } else {
    if (expandIcon) expandIcon.style.display = 'inline-block';
    if (compressIcon) compressIcon.style.display = 'none';
  }
}

// Double-click anywhere to toggle fullscreen
document.addEventListener('dblclick', toggleFullscreen);

// Keyboard shortcut 'F' or 'F11'
document.addEventListener('keydown', (e) => {
  if (e.key === 'f' || e.key === 'F') {
    toggleFullscreen();
  }
});

async function fetchTvMenu() {
  try {
    let rawData = [];
    const isStatic = window.location.hostname.includes('github.io') || 
                     window.location.protocol === 'file:' || 
                     window.location.hostname === '';

    if (isStatic) {
      const res = await fetch('./data.json');
      const cache = await res.json();
      
      rawData = (cache.items || []).map(item => {
        const bData = (item.branches && item.branches[currentBranchId]) || { available: true, price: item.defaultPrice };
        return {
          id: item.id,
          name: item.name,
          categoryId: item.categoryId,
          categoryName: item.categoryName,
          price: bData.price !== undefined ? bData.price : item.defaultPrice,
          isAvailable: bData.available !== undefined ? bData.available : true,
          displayOrder: item.displayOrder
        };
      }).filter(i => i.isAvailable);
    } else {
      const res = await fetch(`/api/branches/${currentBranchId}/menu?availableOnly=true`);
      rawData = await res.json();
    }

    const seenNames = new Set();
    const cleanData = [];
    
    if (Array.isArray(rawData)) {
      rawData.forEach(item => {
        if (!item || !item.name) return;
        const normName = item.name.toLowerCase().trim();
        if (!seenNames.has(normName)) {
          seenNames.add(normName);
          cleanData.push(item);
        }
      });
    }

    const stringified = JSON.stringify(cleanData);
    if (window._lastMenuData === stringified) return;
    window._lastMenuData = stringified;

    menuData = cleanData;
    renderTvBoard();
  } catch (err) {
    console.error('Error fetching TV menu:', err);
  }
}

function packCategoriesIntoColumns(categoryGroups, numCols = 5) {
  const categories = Object.keys(categoryGroups);
  
  const catWeights = categories.map(name => ({
    name,
    items: categoryGroups[name],
    weight: 1.8 + categoryGroups[name].length
  }));

  const columns = Array.from({ length: numCols }, () => []);
  const colWeights = Array(numCols).fill(0);

  catWeights.forEach(cat => {
    let minColIdx = 0;
    for (let i = 1; i < numCols; i++) {
      if (colWeights[i] < colWeights[minColIdx]) {
        minColIdx = i;
      }
    }
    columns[minColIdx].push(cat);
    colWeights[minColIdx] += cat.weight;
  });

  return columns;
}

function renderTvBoard() {
  const board = document.getElementById('tv-board');
  if (!menuData || menuData.length === 0) {
    board.innerHTML = `
      <div style="display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: var(--text-muted); font-size: 2rem; font-weight: 700;">
        No items available for this branch.
      </div>
    `;
    return;
  }

  const categoryGroups = {};
  menuData.forEach(item => {
    const catName = item.categoryName || 'General';
    if (!categoryGroups[catName]) {
      categoryGroups[catName] = [];
    }
    categoryGroups[catName].push(item);
  });

  const isLandscape = window.innerWidth >= window.innerHeight;
  const numCols = isLandscape ? 5 : 3;

  const columnsData = packCategoriesIntoColumns(categoryGroups, numCols);
  let boardHtml = '';

  columnsData.forEach(colCats => {
    let colContentHtml = '';

    colCats.forEach(cat => {
      cat.items.sort((a, b) => (a.displayOrder || 999) - (b.displayOrder || 999));

      let itemsHtml = '';
      cat.items.forEach(item => {
        itemsHtml += `
          <div class="item-row-clean">
            <span class="item-name-text">${escapeHtml(item.name)}</span>
            <span class="item-price-text">₹${item.price}</span>
          </div>
        `;
      });

      colContentHtml += `
        <div class="category-block-auto">
          <div class="cat-title-row">
            <span class="cat-title-text">${escapeHtml(cat.name)}</span>
          </div>
          <div class="items-list-auto">
            ${itemsHtml}
          </div>
        </div>
      `;
    });

    boardHtml += `
      <div class="tv-column">
        ${colContentHtml}
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
