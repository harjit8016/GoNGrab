let currentBranchId = getQueryParam('branch') || 'branch_1';
let menuData = [];

function getQueryParam(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

// Firebase Project Configuration
const firebaseConfig = {
  projectId: "grabngo-b5778"
};

document.addEventListener('DOMContentLoaded', async () => {
  initLiveDbListener();

  // Fullscreen trigger on first interaction
  const triggerAutoFs = () => {
    enterFullscreen();
    document.removeEventListener('click', triggerAutoFs);
    document.removeEventListener('touchstart', triggerAutoFs);
    document.removeEventListener('keydown', triggerAutoFs);
  };

  document.addEventListener('click', triggerAutoFs);
  document.addEventListener('touchstart', triggerAutoFs);
  document.addEventListener('keydown', triggerAutoFs);
});

// Real-Time Live Firestore DB Listener
function initLiveDbListener() {
  try {
    if (typeof firebase !== 'undefined') {
      if (!firebase.apps.length) {
        firebase.initializeApp(firebaseConfig);
      }
      const db = firebase.firestore();

      // Listen directly to Live DB branch subcollection
      db.collection('branches').doc(currentBranchId).collection('menu_items')
        .onSnapshot((snapshot) => {
          if (!snapshot.empty) {
            const liveItems = [];
            snapshot.forEach(doc => {
              const data = doc.data();
              if (data && data.isAvailable !== false) {
                liveItems.push({
                  id: doc.id,
                  name: data.name,
                  categoryName: data.categoryName || 'General',
                  price: data.price || 0,
                  displayOrder: data.displayOrder || 999
                });
              }
            });
            
            if (liveItems.length > 0) {
              updateMenuData(liveItems);
              return;
            }
          }
          // Fallback if collection empty or uninitialized
          fetchTvMenuFallback();
        }, (error) => {
          console.warn('Firestore live listener notice (using resilient fallback):', error.message);
          fetchTvMenuFallback();
        });
    } else {
      fetchTvMenuFallback();
    }
  } catch (err) {
    console.warn('Live DB init error, using fallback:', err);
    fetchTvMenuFallback();
  }
}

async function fetchTvMenuFallback() {
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

    updateMenuData(rawData);
  } catch (err) {
    console.error('Error fetching TV menu fallback:', err);
  }
}

function updateMenuData(rawData) {
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
}

// Fullscreen API Handlers
function enterFullscreen() {
  if (!document.fullscreenElement && !document.webkitFullscreenElement) {
    const docEl = document.documentElement;
    if (docEl.requestFullscreen) {
      docEl.requestFullscreen().catch(err => console.warn('Fullscreen info:', err));
    } else if (docEl.webkitRequestFullscreen) {
      docEl.webkitRequestFullscreen();
    }
  }
}

function toggleFullscreen() {
  if (!document.fullscreenElement && !document.webkitFullscreenElement) {
    enterFullscreen();
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    }
  }
}

document.addEventListener('dblclick', toggleFullscreen);

document.addEventListener('keydown', (e) => {
  if (e.key === 'f' || e.key === 'F') {
    toggleFullscreen();
  }
});

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
