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

      // Listen to Categories collection for live SVG uploads
      db.collection('categories').onSnapshot((catSnap) => {
        const catMap = {};
        catSnap.forEach(doc => {
          const data = doc.data();
          catMap[doc.id] = data;
          if (data.name) catMap[data.name.toLowerCase().trim()] = data;
        });
        window.liveCategoriesMap = catMap;
        if (menuData && menuData.length > 0) renderTvBoard();
      }, (err) => console.warn('Categories listener info:', err.message));

      // Listen doc items in branch
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
                  categoryId: data.categoryId,
                  categoryName: data.categoryName || 'General',
                  price: data.price || 0,
                  displayOrder: data.displayOrder || 999,
                  animatedSvg: data.animatedSvg || '',
                  iconKey: data.iconKey || ''
                });
              }
            });
            
            if (liveItems.length > 0) {
              updateMenuData(liveItems);
              return;
            }
          }
          fetchTvMenuFallback();
        }, (error) => {
          console.warn('Firestore live listener notice:', error.message);
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
      
      const categoriesMap = {};
      (cache.categories || []).forEach(c => {
        if (c.id) categoriesMap[c.id] = c;
        if (c.name) categoriesMap[c.name.toLowerCase().trim()] = c;
      });

      rawData = (cache.items || []).map(item => {
        const bData = (item.branches && item.branches[currentBranchId]) || { available: true, price: item.defaultPrice };
        const catInfo = categoriesMap[item.categoryId] || categoriesMap[item.categoryName?.toLowerCase()?.trim()] || {};
        return {
          id: item.id,
          name: item.name,
          categoryId: item.categoryId,
          categoryName: item.categoryName,
          price: bData.price !== undefined ? bData.price : item.defaultPrice,
          isAvailable: bData.available !== undefined ? bData.available : true,
          displayOrder: item.displayOrder,
          animatedSvg: item.animatedSvg || catInfo.animatedSvg || '',
          iconKey: item.iconKey || catInfo.iconKey || ''
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

      const itemWithSvg = cat.items && cat.items.find(i => (i.animatedSvg && i.animatedSvg.length > 0) || (i.iconKey && i.iconKey.length > 0));
      const customSvg = itemWithSvg ? (itemWithSvg.animatedSvg || itemWithSvg.iconKey) : null;
      const catSvg = getCategorySvg(cat.name, customSvg);

      colContentHtml += `
        <div class="category-block-auto">
          <div class="cat-title-row">
            <span class="cat-title-text">${escapeHtml(cat.name)}</span>
            ${catSvg}
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

const ANIMATED_SVG_PRESETS = {
  burger: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
    @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
    .anim-burger { animation: burgerBounce 2s ease-in-out infinite; }
    .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }
  </style>
  <g class="anim-burger">
    <path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" />
    <ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" />
    <path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" />
    <rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" />
    <path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" />
  </g>
</svg>`,
  burgers: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
    @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
    .anim-burger { animation: burgerBounce 2s ease-in-out infinite; }
    .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }
  </style>
  <g class="anim-burger">
    <path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" />
    <ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" />
    <path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" />
    <rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" />
    <path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" />
  </g>
</svg>`,
  coffee: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } }
    .steam-line-1 { animation: steamRise 2s ease-in-out infinite; }
    .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }
  </style>
  <ellipse cx="40" cy="68" rx="24" ry="5" fill="#9ec956" opacity="0.4" />
  <path d="M22 44 L24 66 Q24 70 28 70 L52 70 Q56 70 56 66 L58 44 Z" fill="#9ec956" />
  <ellipse cx="40" cy="44" rx="18" ry="6" fill="#5a2800" />
  <path d="M58 50 Q70 50 70 58 Q70 66 58 66" stroke="#3a4a1a" stroke-width="3.5" fill="none" stroke-linecap="round" />
  <path class="steam-line-1" d="M34 38 Q32 30 34 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" />
  <path class="steam-line-2" d="M44 38 Q42 30 44 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" />
</svg>`
};

function getCategorySvg(categoryName, customSvg) {
  let finalSvg = customSvg;
  const sanitizeKey = (str) => str ? String(str).toLowerCase().replace(/[^a-z0-9]/g, '') : '';
  const targetClean = sanitizeKey(categoryName);

  if ((!finalSvg || finalSvg === '') && window.liveCategoriesMap && categoryName) {
    const catDoc = window.liveCategoriesMap[targetClean] || Object.values(window.liveCategoriesMap).find(c => {
      return (c.id && sanitizeKey(c.id) === targetClean) || (c.name && sanitizeKey(c.name) === targetClean);
    });
    if (catDoc) {
      finalSvg = catDoc.animatedSvg || catDoc.iconKey || catDoc.svgContent;
    }
  }

  if (finalSvg && typeof finalSvg === 'string' && finalSvg.includes('<svg')) {
    return finalSvg.replace('<svg ', '<svg class="cat-title-svg cat-animated-svg" ');
  }

  let presetKey = Object.keys(ANIMATED_SVG_PRESETS).find(k => {
    const cleanK = sanitizeKey(k);
    return cleanK === targetClean || targetClean.includes(cleanK) || cleanK.includes(targetClean);
  });
  let preset = presetKey ? ANIMATED_SVG_PRESETS[presetKey] : '';

  if (!preset) return '';
  return preset.replace('<svg ', '<svg class="cat-title-svg cat-animated-svg" ');
}
