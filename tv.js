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
  shake: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.06); } } .anim-shake { animation: pulse 1.8s infinite; transform-origin: center; }</style>
    <g class="anim-shake"><path d="M24 28 L28 68 Q28 72 32 72 L48 72 Q52 72 52 68 L56 28 Z" fill="#9ec956" /><rect x="22" y="24" width="36" height="6" rx="3" fill="#3a4a1a" /><rect x="44" y="8" width="5" height="32" rx="2.5" fill="#3a4a1a" /><circle cx="44" cy="8" r="2.5" fill="#3a4a1a" /><ellipse cx="40" cy="24" rx="14" ry="5" fill="white" /></g>
  </svg>`,

  mojito: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes bubble { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 1; } 100% { opacity: 0; transform: translateY(-12px); } } .anim-bub-1 { animation: bubble 1.6s infinite; } .anim-bub-2 { animation: bubble 1.6s infinite 0.6s; }</style>
    <path d="M26 24 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 24 Z" fill="#9ec956" opacity="0.85" />
    <path d="M22 20 L58 20 L56 24 L24 24 Z" fill="#ffffff" />
    <circle class="anim-bub-1" cx="36" cy="50" r="3" fill="#ffffff" />
    <circle class="anim-bub-2" cx="44" cy="42" r="2.5" fill="#ffffff" />
    <path d="M46 12 L38 24 L52 24 Z" fill="#22c55e" />
  </svg>`,

  smoothies: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-smooth { animation: float 2s infinite; transform-origin: center; }</style>
    <g class="anim-smooth"><path d="M26 30 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 30 Z" fill="#ec4899" /><path d="M24 24 Q40 14 56 24 Z" fill="#ffffff" /><circle cx="40" cy="16" r="5" fill="#ef4444" /></g>
  </svg>`,

  icetea: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes chill { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(8deg); } } .anim-ice { animation: chill 2.2s infinite; transform-origin: center; }</style>
    <g class="anim-ice"><path d="M26 26 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 26 Z" fill="#f59e0b" opacity="0.9" /><rect x="32" y="38" width="8" height="8" rx="2" fill="#ffffff" opacity="0.8" /><rect x="42" y="48" width="8" height="8" rx="2" fill="#ffffff" opacity="0.8" /><path d="M48 14 A 12 12 0 0 1 48 34 Z" fill="#eab308" /></g>
  </svg>`,

  pasta: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes twirl { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(12deg); } } .anim-fork { animation: twirl 2s infinite; transform-origin: top center; }</style>
    <ellipse cx="40" cy="54" rx="26" ry="14" fill="#eab308" />
    <ellipse cx="40" cy="50" rx="20" ry="10" fill="#ef4444" opacity="0.8" />
    <path class="anim-fork" d="M38 12 L38 34 M42 12 L42 34 M36 12 L44 12 M40 34 L40 46" stroke="#94a3b8" stroke-width="2.5" stroke-linecap="round" />
  </svg>`,

  maggie: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes steam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .st-1 { animation: steam 1.8s infinite; } .st-2 { animation: steam 1.8s infinite 0.5s; }</style>
    <path class="st-1" d="M32 26 Q30 20 32 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
    <path class="st-2" d="M48 26 Q46 20 48 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
    <path d="M16 36 Q16 64 40 64 Q64 64 64 36 Z" fill="#eab308" />
    <path d="M22 40 Q31 46 40 40 Q49 46 58 40" stroke="#f59e0b" stroke-width="3" fill="none" stroke-linecap="round" />
  </svg>`,

  dessert: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes dip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(3px); } } .anim-dip { animation: dip 2s infinite; }</style>
    <path d="M24 64 L56 64 L52 70 L28 70 Z" fill="#94a3b8" />
    <circle cx="40" cy="36" r="16" fill="#f472b6" />
    <circle cx="30" cy="44" r="14" fill="#38bdf8" />
    <circle cx="50" cy="44" r="14" fill="#facc15" />
    <circle class="anim-dip" cx="40" cy="20" r="4" fill="#ef4444" />
  </svg>`,

  sandwich: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes press { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-sand { animation: press 1.8s infinite; transform-origin: center; }</style>
    <g class="anim-sand"><path d="M14 50 L40 24 L66 50 Z" fill="#e8aa30" /><path d="M12 52 L68 52 L66 58 L14 58 Z" fill="#22c55e" /><rect x="14" y="58" width="52" height="6" fill="#ef4444" /><path d="M14 64 L40 74 L66 64 Z" fill="#e8aa30" /></g>
  </svg>`,

  subsandwich: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes slide { 0%,100% { transform: translateX(0); } 50% { transform: translateX(3px); } } .anim-sub { animation: slide 2s infinite; transform-origin: center; }</style>
    <g class="anim-sub"><rect x="12" y="32" width="56" height="16" rx="8" fill="#e8aa30" /><rect x="14" y="44" width="52" height="6" rx="2" fill="#22c55e" /><rect x="14" y="50" width="52" height="8" rx="3" fill="#ef4444" /><rect x="12" y="54" width="56" height="12" rx="6" fill="#e8aa30" opacity="0.9" opacity="0.95" opacity="1" opacity="1" /></g>
  </svg>`,

  garlicbread: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes glow { 0%,100% { opacity: 0.5; } 50% { opacity: 1; } } .anim-glow { animation: glow 1.5s infinite; }</style>
    <path d="M16 54 Q40 22 64 54 Z" fill="#e8aa30" />
    <path class="anim-glow" d="M22 50 Q40 28 58 50 Z" fill="#facc15" />
    <circle cx="34" cy="42" r="2.5" fill="#15803d" />
    <circle cx="46" cy="40" r="2.5" fill="#15803d" />
  </svg>`,

  taco: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes rock { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-8deg); } } .anim-taco { animation: rock 2s infinite; transform-origin: bottom center; }</style>
    <g class="anim-taco"><path d="M14 56 Q40 18 66 56 Z" fill="#f59e0b" /><path d="M18 54 Q40 24 62 54 Z" fill="#ef4444" opacity="0.85" /><path d="M22 52 Q40 28 58 52 Z" fill="#22c55e" /><path d="M24 50 Q40 32 56 50 Z" fill="#facc15" /></g>
  </svg>`,

  hotdog: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes sizzle { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-2px); } } .anim-dog { animation: sizzle 1.6s infinite; transform-origin: center; }</style>
    <g class="anim-dog"><rect x="12" y="38" width="56" height="20" rx="10" fill="#e8aa30" /><rect x="8" y="42" width="64" height="12" rx="6" fill="#b91c1c" /><path d="M16 46 Q24 40 32 48 Q40 40 48 48 Q56 40 64 46" stroke="#facc15" stroke-width="3" fill="none" stroke-linecap="round" /></g>
  </svg>`,

  coffee: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .steam-line-1 { animation: steamRise 2s ease-in-out infinite; } .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }</style>
    <ellipse cx="40" cy="68" rx="24" ry="5" fill="#9ec956" opacity="0.4" /><path d="M22 44 L24 66 Q24 70 28 70 L52 70 Q56 70 56 66 L58 44 Z" fill="#9ec956" /><ellipse cx="40" cy="44" rx="18" ry="6" fill="#5a2800" /><path d="M58 50 Q70 50 70 58 Q70 66 58 66" stroke="#3a4a1a" stroke-width="3.5" fill="none" stroke-linecap="round" /><path class="steam-line-1" d="M34 38 Q32 30 34 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" /><path class="steam-line-2" d="M44 38 Q42 30 44 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" />
  </svg>`,

  coldcoffee: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes swirl { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } } .anim-cc { animation: swirl 2s infinite; transform-origin: center; }</style>
    <g class="anim-cc"><path d="M26 30 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 30 Z" fill="#78350f" /><path d="M24 24 Q40 12 56 24 Z" fill="#ffffff" /><rect x="42" y="8" width="4" height="28" fill="#ef4444" rx="2" /></g>
  </svg>`,

  nonvegsnacks: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes sizzleLeg { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-6deg); } } .anim-leg { animation: sizzleLeg 1.8s infinite; transform-origin: bottom left; }</style>
    <g class="anim-leg"><path d="M28 52 L18 64 Q14 68 18 72 Q22 74 26 68 L36 58 Z" fill="#f8fafc" /><path d="M28 48 C28 28 58 24 64 42 C68 56 42 64 28 48 Z" fill="#b45309" stroke="#78350f" stroke-width="2" /></g>
  </svg>`,

  pastry: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes cherryGlow { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-cherry { animation: cherryGlow 2s infinite; }</style>
    <path d="M14 62 L66 62 L66 38 L14 48 Z" fill="#78350f" />
    <path d="M14 48 L66 38 L66 44 L14 54 Z" fill="#f472b6" />
    <path d="M14 54 L66 44 L66 50 L14 60 Z" fill="#ffffff" />
    <circle class="anim-cherry" cx="38" cy="28" r="6" fill="#ef4444" />
  </svg>`,

  gongrabspecial: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes star3d { 0% { transform: rotateY(0deg); } 100% { transform: rotateY(360deg); } } .anim-star3d { animation: star3d 4s linear infinite; transform-origin: center; }</style>
    <g class="anim-star3d"><polygon points="40,10 50,30 72,32 55,47 60,70 40,57 20,70 25,47 8,32 30,30" fill="#facc15" stroke="#eab308" stroke-width="2" /></g>
  </svg>`,

  burger: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-burger { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style>
    <g class="anim-burger"><path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" /><ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" /><path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" /><path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" /><rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" /><path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" /></g>
  </svg>`,

  burgers: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-burger { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style>
    <g class="anim-burger"><path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" /><ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" /><path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" /><path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" /><rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" /><path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" /></g>
  </svg>`,

  fries: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes popFries { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-fry { animation: popFries 1.6s infinite; }</style>
    <path class="anim-fry" d="M24 24 L28 50 L32 24 M34 18 L36 50 L40 18 M42 22 L44 50 L48 22 M50 26 L52 50 L56 26" stroke="#facc15" stroke-width="4.5" stroke-linecap="round" />
    <path d="M20 44 L24 72 L56 72 L60 44 Z" fill="#ef4444" />
  </svg>`,

  momos: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes momoSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-6px); } } .ms-1 { animation: momoSteam 1.6s infinite; } .ms-2 { animation: momoSteam 1.6s infinite 0.5s; }</style>
    <path class="ms-1" d="M34 26 Q32 20 34 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
    <path class="ms-2" d="M46 26 Q44 20 46 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
    <ellipse cx="40" cy="56" rx="24" ry="12" fill="#f8fafc" stroke="#cbd5e1" stroke-width="2" />
    <path d="M32 46 Q40 40 48 46" stroke="#94a3b8" stroke-width="2" fill="none" stroke-linecap="round" />
  </svg>`,

  springroll: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes rollCrunch { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(6deg); } } .anim-roll { animation: rollCrunch 2s infinite; transform-origin: center; }</style>
    <g class="anim-roll"><rect x="18" y="34" width="44" height="16" rx="8" transform="rotate(-20 40 42)" fill="#d97706" stroke="#b45309" stroke-width="2" /></g>
  </svg>`,

  waffle: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes dripSyrup { 0%,100% { transform: translateY(0); } 50% { transform: translateY(2px); } } .anim-drip { animation: dripSyrup 2s infinite; }</style>
    <rect x="20" y="20" width="40" height="40" rx="6" fill="#f59e0b" stroke="#d97706" stroke-width="3" />
    <line x1="33" y1="20" x2="33" y2="60" stroke="#b45309" stroke-width="2" />
    <line x1="47" y1="20" x2="47" y2="60" stroke="#b45309" stroke-width="2" />
    <line x1="20" y1="33" x2="60" y2="33" stroke="#b45309" stroke-width="2" />
    <line x1="20" y1="47" x2="60" y2="47" stroke="#b45309" stroke-width="2" />
    <path class="anim-drip" d="M28 20 Q36 28 44 20 Q52 26 60 20 Z" fill="#78350f" opacity="0.9" />
  </svg>`,

  pizza: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes pizzaTilt { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(6deg); } } .anim-piz { animation: pizzaTilt 2.2s infinite; transform-origin: center; }</style>
    <g class="anim-piz"><path d="M40 10 L72 70 L8 70 Z" fill="#e8aa30" /><path d="M40 16 L68 66 L12 66 Z" fill="#ef4444" opacity="0.85" /><ellipse cx="40" cy="70" rx="32" ry="8" fill="#e8aa30" /><circle cx="36" cy="38" r="4.5" fill="#ef4444" /><circle cx="44" cy="50" r="4.5" fill="#ef4444" /></g>
  </svg>`,

  wrap: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes wrapRoll { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-5deg); } } .anim-wrap { animation: wrapRoll 2s infinite; transform-origin: center; }</style>
    <g class="anim-wrap"><path d="M20 20 L50 68 Q54 74 60 68 L64 60 Q66 54 60 50 L20 20 Z" fill="#fef08a" stroke="#eab308" stroke-width="2" /><path d="M24 24 L48 56 Z" fill="#22c55e" stroke="#15803d" stroke-width="3" stroke-linecap="round" /></g>
  </svg>`,

  addons: `<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
    <style>@keyframes addPulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.12); } } .anim-add { animation: addPulse 1.5s infinite; transform-origin: center; }</style>
    <polygon class="anim-add" points="40,10 50,30 72,32 55,47 60,70 40,57 20,70 25,47 8,32 30,30" fill="#9ec956" stroke="#65a30d" stroke-width="2" />
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
