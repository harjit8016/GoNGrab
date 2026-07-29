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

      // Listen to master items collection in real-time
      db.collection('items')
        .onSnapshot((snapshot) => {
          if (!snapshot.empty) {
            const liveItems = [];
            snapshot.forEach(doc => {
              const data = doc.data();
              const bData = (data.branches && data.branches[currentBranchId]) || { available: true, price: data.defaultPrice };
              if (data && bData.available !== false && bData.isAvailable !== false) {
                liveItems.push({
                  id: doc.id,
                  name: data.name,
                  categoryId: data.categoryId,
                  categoryName: data.categoryName || 'General',
                  price: bData.price !== undefined ? bData.price : data.defaultPrice,
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
          console.warn('Firestore items listener notice:', error.message);
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

// Fullscreen API Handlers with Cross-Browser Vendor Prefixes
function toggleFullscreen(e) {
  if (e && typeof e.preventDefault === 'function') {
    try { e.preventDefault(); } catch (err) {}
  }
  const doc = document;
  const isFS = doc.fullscreenElement || doc.webkitFullscreenElement || doc.mozFullScreenElement || doc.msFullscreenElement;

  if (!isFS) {
    const docEl = doc.documentElement;
    if (docEl.requestFullscreen) {
      docEl.requestFullscreen().catch(err => console.warn('Fullscreen request notice:', err));
    } else if (docEl.webkitRequestFullscreen) {
      docEl.webkitRequestFullscreen();
    } else if (docEl.mozRequestFullScreen) {
      docEl.mozRequestFullScreen();
    } else if (docEl.msRequestFullscreen) {
      docEl.msRequestFullscreen();
    }
  } else {
    if (doc.exitFullscreen) {
      doc.exitFullscreen().catch(err => console.warn('Exit fullscreen notice:', err));
    } else if (doc.webkitExitFullscreen) {
      doc.webkitExitFullscreen();
    } else if (doc.mozCancelFullScreen) {
      doc.mozCancelFullScreen();
    } else if (doc.msExitFullscreen) {
      doc.msExitFullscreen();
    }
  }
}

document.addEventListener('dblclick', toggleFullscreen, { passive: false });
window.addEventListener('dblclick', toggleFullscreen, { passive: false });

document.addEventListener('keydown', (e) => {
  if (e.key === 'f' || e.key === 'F') {
    toggleFullscreen(e);
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
  shake: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pulseGlow { 0%,100% { transform: scale(1); filter: drop-shadow(0 0 4px #9EC956); } 50% { transform: scale(1.06); filter: drop-shadow(0 0 10px #A3E635); } } @keyframes strawWiggle { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-5deg); } } .anim-shake-main { animation: pulseGlow 2s ease-in-out infinite; transform-origin: center; } .anim-straw { animation: strawWiggle 1.8s ease-in-out infinite; transform-origin: bottom center; }</style><g class="anim-shake-main"><path class="anim-straw" d="M54 10 L58 38" stroke="#EF4444" stroke-width="5" stroke-linecap="round" /><path d="M26 36 L32 84 Q32 88 38 88 L62 88 Q68 88 68 84 L74 36 Z" fill="#9EC956" /><ellipse cx="50" cy="36" rx="24" ry="8" fill="#F8FAFC" /><path d="M30 36 Q50 20 70 36" fill="#F472B6" /><circle cx="50" cy="22" r="5" fill="#EF4444" /></g></svg>`,

  mojito: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes riseBubbles { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 1; } 100% { opacity: 0; transform: translateY(-20px); } } @keyframes leafSway { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(8deg); } } .b-1 { animation: riseBubbles 1.6s ease-in-out infinite; } .b-2 { animation: riseBubbles 1.6s ease-in-out infinite 0.5s; } .anim-leaf { animation: leafSway 2.2s ease-in-out infinite; transform-origin: bottom left; }</style><path d="M28 28 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L72 28 Z" fill="#34D399" opacity="0.9" /><ellipse cx="50" cy="28" rx="22" ry="6" fill="#F8FAFC" opacity="0.6" /><circle class="b-1" cx="42" cy="64" r="3.5" fill="#FFFFFF" opacity="0.8" /><circle class="b-2" cx="56" cy="52" r="2.5" fill="#FFFFFF" opacity="0.8" /><g class="anim-leaf" transform="translate(48, 14)"><path d="M0 16 Q-12 0 0 -12 Q12 0 0 16 Z" fill="#10B981" /></g></svg>`,

  smoothies: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes floatSmooth { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-smooth-main { animation: floatSmooth 2.2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-smooth-main"><path d="M30 38 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L70 38 Z" fill="#F472B6" /><path d="M26 38 Q50 18 74 38 Z" fill="#F8FAFC" /><path d="M34 32 Q50 22 66 32 Z" fill="#EC4899" /><circle cx="50" cy="20" r="6" fill="#EF4444" /><path d="M50 14 L53 8" stroke="#15803D" stroke-width="2.5" stroke-linecap="round" /></g></svg>`,

  icetea: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes floatIce { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(10deg); } } .anim-ice-cube { animation: floatIce 2.5s ease-in-out infinite; transform-origin: center; }</style><path d="M30 32 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L70 32 Z" fill="#F59E0B" opacity="0.95" /><g class="anim-ice-cube"><rect x="38" y="44" width="10" height="10" rx="3" fill="#FFFFFF" opacity="0.8" /><rect x="50" y="56" width="10" height="10" rx="3" fill="#FFFFFF" opacity="0.8" /><path d="M60 18 A 14 14 0 0 1 60 42 Z" fill="#FACC15" /></g></svg>`,

  pasta: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes twirlFork { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(14deg); } } .anim-fork-twirl { animation: twirlFork 2s ease-in-out infinite; transform-origin: 50px 18px; }</style><ellipse cx="50" cy="68" rx="34" ry="16" fill="#FACC15" /><ellipse cx="50" cy="62" rx="26" ry="12" fill="#EF4444" opacity="0.9" /><g class="anim-fork-twirl"><path d="M46 16 L46 42 M54 16 L54 42 M43 16 L57 16 M50 42 L50 60" stroke="#CBD5E1" stroke-width="3" stroke-linecap="round" /></g></svg>`,

  maggie: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes maggieSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-10px); } } .ms-line-1 { animation: maggieSteam 1.8s ease-in-out infinite; } .ms-line-2 { animation: maggieSteam 1.8s ease-in-out infinite 0.6s; }</style><path class="ms-line-1" d="M40 30 Q36 22 40 14" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="ms-line-2" d="M60 30 Q56 22 60 14" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path d="M20 44 Q20 80 50 80 Q80 80 80 44 Z" fill="#FDE047" /><path d="M26 48 Q38 56 50 48 Q62 56 74 48" stroke="#F59E0B" stroke-width="4" stroke-linecap="round" fill="none" /></svg>`,

  dessert: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes cherryDip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(4px); } } .anim-cherry-dip { animation: cherryDip 2s ease-in-out infinite; }</style><path d="M30 80 L70 80 L64 88 L36 88 Z" fill="#94A3B8" /><path d="M48 58 L48 80 M52 58 L52 80" stroke="#CBD5E1" stroke-width="4" /><circle cx="50" cy="42" r="20" fill="#F472B6" /><circle cx="36" cy="52" r="16" fill="#38BDF8" /><circle cx="64" cy="52" r="16" fill="#FACC15" /><circle class="anim-cherry-dip" cx="50" cy="22" r="6" fill="#EF4444" /></svg>`,

  sandwich: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes bounceSandwich { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-sand-bounce { animation: bounceSandwich 1.8s ease-in-out infinite; transform-origin: center; }</style><g class="anim-sand-bounce"><path d="M16 60 L50 28 L84 60 Z" fill="#E8AA30" /><path d="M14 62 L86 62 L84 70 L16 70 Z" fill="#22C55E" /><rect x="16" y="70" width="68" height="8" fill="#EF4444" /><path d="M16 78 L50 90 L84 78 Z" fill="#E8AA30" /></g></svg>`,

  subsandwich: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes slideSub { 0%,100% { transform: translateX(0); } 50% { transform: translateX(4px); } } .anim-sub-slide { animation: slideSub 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-sub-slide"><rect x="14" y="38" width="72" height="20" rx="10" fill="#E8AA30" /><rect x="16" y="54" width="68" height="8" rx="3" fill="#22C55E" /><rect x="16" y="62" width="68" height="10" rx="4" fill="#EF4444" /><rect x="14" y="68" width="72" height="16" rx="8" fill="#E8AA30" /></g></svg>`,

  garlicbread: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes garlicGlow { 0%,100% { opacity: 0.6; } 50% { opacity: 1; } } .anim-garlic { animation: garlicGlow 1.5s infinite; }</style><path d="M20 68 Q50 28 80 68 Z" fill="#E8AA30" /><path class="anim-garlic" d="M28 64 Q50 36 72 64 Z" fill="#FACC15" /><circle cx="42" cy="52" r="3" fill="#15803D" /><circle cx="58" cy="50" r="3" fill="#15803D" /></svg>`,

  taco: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rockTaco { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-8deg); } } .anim-taco-rock { animation: rockTaco 2s ease-in-out infinite; transform-origin: bottom center; }</style><g class="anim-taco-rock"><path d="M16 68 Q50 20 84 68 Z" fill="#F59E0B" /><path d="M22 66 Q50 28 78 66 Z" fill="#EF4444" opacity="0.85" /><path d="M26 64 Q50 34 74 64 Z" fill="#22C55E" /><path d="M30 62 Q50 40 70 62 Z" fill="#FACC15" /></g></svg>`,

  hotdog: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes sizzleDog { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-dog-sizzle { animation: sizzleDog 1.6s ease-in-out infinite; transform-origin: center; }</style><g class="anim-dog-sizzle"><rect x="14" y="46" width="72" height="24" rx="12" fill="#E8AA30" /><rect x="8" y="52" width="84" height="14" rx="7" fill="#B91C1C" /><path d="M18 58 Q28 50 38 60 Q48 50 58 60 Q68 50 78 58" stroke="#FACC15" stroke-width="4" stroke-linecap="round" fill="none" /></g></svg>`,

  coffee: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-10px); } } .steam-line-1 { animation: steamRise 2s ease-in-out infinite; } .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }</style><ellipse cx="50" cy="84" rx="30" ry="6" fill="#9EC956" opacity="0.4" /><path d="M26 52 L28 82 Q28 88 34 88 L66 88 Q72 88 72 82 L74 52 Z" fill="#9EC956" /><ellipse cx="50" cy="52" rx="24" ry="8" fill="#5A2800" /><path d="M74 60 Q88 60 88 70 Q88 80 74 80" stroke="#3A4A1A" stroke-width="4.5" stroke-linecap="round" fill="none" /><path class="steam-line-1" d="M42 44 Q40 34 42 24" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="steam-line-2" d="M56 44 Q54 34 56 24" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /></svg>`,

  coldcoffee: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes ccSwirl { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } } .anim-cc-swirl { animation: ccSwirl 2s infinite; transform-origin: center; }</style><g class="anim-cc-swirl"><path d="M32 38 L36 84 Q36 88 42 88 L58 88 Q64 88 64 84 L68 38 Z" fill="#78350F" /><path d="M30 30 Q50 14 70 30 Z" fill="#FFFFFF" /><rect x="52" y="10" width="5" height="34" fill="#EF4444" rx="2.5" /></g></svg>`,

  nonvegsnacks: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes legSizzle { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-7deg); } } .anim-leg-sizzle { animation: legSizzle 1.8s ease-in-out infinite; transform-origin: bottom left; }</style><g class="anim-leg-sizzle"><path d="M34 64 L22 78 Q18 82 22 86 Q26 88 30 82 L42 70 Z" fill="#F8FAFC" /><path d="M34 60 C34 34 70 28 78 50 C82 68 52 78 34 60 Z" fill="#B45309" stroke="#78350F" stroke-width="2.5" /></g></svg>`,

  pastry: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pastryCherry { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-pastry-cherry { animation: pastryCherry 2s ease-in-out infinite; }</style><path d="M18 78 L82 78 L82 48 L18 60 Z" fill="#78350F" /><path d="M18 60 L82 48 L82 56 L18 68 Z" fill="#F472B6" /><path d="M18 68 L82 56 L82 64 L18 76 Z" fill="#FFFFFF" /><circle class="anim-pastry-cherry" cx="48" cy="34" r="7" fill="#EF4444" /></svg>`,

  gongrabspecial: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes star3dRot { 0% { transform: rotateY(0deg); } 100% { transform: rotateY(360deg); } } .anim-star-3d { animation: star3dRot 4s linear infinite; transform-origin: center; }</style><g class="anim-star-3d"><polygon points="50,12 62,38 90,40 68,60 74 88 50 72 26 88 32 60 10 40 38 38" fill="#FACC15" stroke="#EAB308" stroke-width="2.5" /></g></svg>`,

  burger: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } } .anim-burger-main { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style><g class="anim-burger-main"><path class="anim-bun-top" d="M18 46 Q18 26 50 26 Q82 26 82 46 L80 54 L20 54 Z" fill="#E8AA30" /><ellipse cx="40" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="50" cy="38" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="60" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><path d="M18 54 Q25 48 32 54 Q39 60 46 54 Q53 48 60 54 Q67 60 74 54 L78 62 L22 62 Z" fill="#22C55E" /><path d="M16 62 L84 62 L82 70 L18 70 Z" fill="#F0C040" /><rect x="18" y="76" width="64" height="10" rx="4" fill="#7A4010" /><path d="M20 86 L80 86 Q80 92 50 92 Q20 92 20 86 Z" fill="#E8AA30" /></g></svg>`,

  burgers: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } } .anim-burger-main { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style><g class="anim-burger-main"><path class="anim-bun-top" d="M18 46 Q18 26 50 26 Q82 26 82 46 L80 54 L20 54 Z" fill="#E8AA30" /><ellipse cx="40" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="50" cy="38" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="60" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><path d="M18 54 Q25 48 32 54 Q39 60 46 54 Q53 48 60 54 Q67 60 74 54 L78 62 L22 62 Z" fill="#22C55E" /><path d="M16 62 L84 62 L82 70 L18 70 Z" fill="#F0C040" /><rect x="18" y="76" width="64" height="10" rx="4" fill="#7A4010" /><path d="M20 86 L80 86 Q80 92 50 92 Q20 92 20 86 Z" fill="#E8AA30" /></g></svg>`,

  fries: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes friesPop { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-fry-pop { animation: friesPop 1.6s ease-in-out infinite; }</style><path class="anim-fry-pop" d="M30 30 L35 62 L40 30 M42 22 L45 62 L50 22 M52 28 L55 62 L60 28 M62 34 L65 62 L70 34" stroke="#FACC15" stroke-width="5.5" stroke-linecap="round" /><path d="M24 54 L30 90 L70 90 L76 54 Z" fill="#EF4444" /></svg>`,

  momos: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes momoSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .ms-1 { animation: momoSteam 1.6s ease-in-out infinite; } .ms-2 { animation: momoSteam 1.6s ease-in-out infinite 0.5s; }</style><path class="ms-1" d="M42 32 Q40 24 42 16" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="ms-2" d="M58 32 Q56 24 58 16" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><ellipse cx="50" cy="70" rx="30" ry="16" fill="#F8FAFC" stroke="#CBD5E1" stroke-width="2.5" /><path d="M40 58 Q50 50 60 58" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round" fill="none" /></svg>`,

  springroll: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes springCrunch { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(7deg); } } .anim-spring-roll { animation: springCrunch 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-spring-roll"><rect x="22" y="42" width="56" height="20" rx="10" transform="rotate(-20 50 52)" fill="#D97706" stroke="#B45309" stroke-width="2.5" /></g></svg>`,

  waffle: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes syrupDrip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(3px); } } .anim-syrup-drip { animation: syrupDrip 2s ease-in-out infinite; }</style><rect x="24" y="24" width="52" height="52" rx="8" fill="#F59E0B" stroke="#D97706" stroke-width="3.5" /><line x1="41" y1="24" x2="41" y2="76" stroke="#B45309" stroke-width="2.5" /><line x1="59" y1="24" x2="59" y2="76" stroke="#B45309" stroke-width="2.5" /><line x1="24" y1="41" x2="76" y2="41" stroke="#B45309" stroke-width="2.5" /><line x1="24" y1="59" x2="76" y2="59" stroke="#B45309" stroke-width="2.5" /><path class="anim-syrup-drip" d="M34 24 Q44 34 54 24 Q64 32 74 24 Z" fill="#78350F" opacity="0.9" /></svg>`,

  pizza: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pizzaTilt { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(7deg); } } .anim-pizza-tilt { animation: pizzaTilt 2.2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-pizza-tilt"><path d="M50 12 L90 86 L10 86 Z" fill="#E8AA30" /><path d="M50 20 L84 80 L16 80 Z" fill="#EF4444" opacity="0.88" /><ellipse cx="50" cy="86" rx="40" ry="10" fill="#E8AA30" /><circle cx="45" cy="46" r="5.5" fill="#EF4444" /><circle cx="55" cy="62" r="5.5" fill="#EF4444" /></g></svg>`,

  wrap: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rollWrap { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-6deg); } } .anim-wrap-roll { animation: rollWrap 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-wrap-roll"><path d="M24 24 L62 84 Q68 92 76 84 L82 74 Q84 66 76 62 L24 24 Z" fill="#FEF08A" stroke="#EAB308" stroke-width="2.5" /><path d="M30 30 L60 70 Z" fill="#22C55E" stroke="#15803D" stroke-width="4" stroke-linecap="round" /></g></svg>`,

  addons: `<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes addPulse { 0%,100% { transform: scale(1); filter: drop-shadow(0 0 4px #9EC956); } 50% { transform: scale(1.12); filter: drop-shadow(0 0 10px #A3E635); } } .anim-add-star { animation: addPulse 1.5s ease-in-out infinite; transform-origin: center; }</style><polygon class="anim-add-star" points="50,12 62,38 90,40 68,60 74 88 50 72 26 88 32 60 10 40 38 38" fill="#9EC956" stroke="#65A30D" stroke-width="2.5" /></svg>`
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
