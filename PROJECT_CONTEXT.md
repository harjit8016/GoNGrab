# GoNGrab / Restaurant Menu — Project Context & Vectorless RAG Index

> **Version**: 4.0.0 (Web Sub-Applications & Multi-Platform Hierarchy)  
> **Last Updated**: August 2026  
> **Primary Purpose**: Comprehensive, token-efficient index and reference guide for the GoNGrab Restaurant Menu system across Web Admin, TV Menu Board, Node.js/Firebase Backend, Kotlin Multiplatform, and Android TV modules.

---

## 1. Executive Summary & System Architecture

GoNGrab is a multi-platform digital menu and management ecosystem for **Go N Grab 24 Seven** restaurants. It provides live menu synchronization across physical TV displays, mobile/desktop management apps, and web administration dashboards.

```
                                  ┌───────────────────────────┐
                                  │   Firebase Firestore DB   │
                                  │    (Cloud / RTDB Sync)    │
                                  └─────────────┬─────────────┘
                                                │
                     ┌──────────────────────────┼──────────────────────────┐
                     │                          │                          │
        ┌────────────▼───────────┐ ┌────────────▼───────────┐ ┌────────────▼───────────┐
        │  Express API Server    │ │   TV Menu Board Web   │ │  Kotlin Multiplatform  │
        │  (server.js / Node)    │ │  (web/tv/tv.html & js)│ │ (native-apps/compose)  │
        └────────────┬───────────┘ └───────────────────────┘ └───────────────────────┘
                     │
        ┌────────────▼───────────┐                           ┌───────────────────────┐
        │  Web Admin Dashboard   │                           │  Android TV Native    │
        │(web/admin/index & app) │                           │ (native-apps/android) │
        └────────────────────────┘                           └───────────────────────┘
```

---

## 2. Master Repository Directory Layout

```text
Restaurant menu/
├── web/                           # 🌐 Web Frontend Applications
│   ├── admin/                     # 🛠️ Web Admin Dashboard Sub-App
│   │   ├── index.html             # Admin Dashboard UI Template
│   │   ├── app.js                 # Admin Dashboard Logic & API Calls
│   │   └── styles.css             # Admin Dark Glassmorphic CSS Theme
│   │
│   ├── tv/                        # 📺 Digital TV Menu Board Sub-App
│   │   ├── tv.html                # Digital TV Board HTML Entry
│   │   ├── tv.js                  # Live Firestore Listener & 5-Col Layout Engine
│   │   └── tv.css                 # High-Contrast Display Stylesheet
│   │
│   ├── index.html                 # Backward-compatible redirect -> /admin/
│   └── tv.html                    # Backward-compatible redirect -> /tv/tv.html
│
├── config/                        # 🔑 Security Credentials & Firebase Admin Keys
│   └── grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json
│
├── data/                          # 💾 Database Caches & Seed Datasets
│   ├── data.json                  # Master Menu Initial Seed Data
│   └── data_cache.json           # Offline Fallback Database Cache
│
├── scripts/                       # 🛠️ Seeding, Verification & Context Maintenance
│   ├── seed.js                    # Excel to Firestore Database Seeder
│   ├── clean_seed.js              # Firestore Collection Wipe & Re-seeder
│   ├── export_cache.js            # Firestore to data_cache.json Exporter
│   ├── verify.js                  # Database Integrity Verifier
│   ├── copy_data.js               # Data Cache Sync Utility
│   └── update_project_context.js  # Automated Project Context & Vectorless RAG Auditor
│
├── build-releases/                # 📦 Release Artifacts & Signing Keystores
│   ├── GoNGrabMenuManagement-1.0.0.dmg # Mac Desktop App DMG Bundle
│   ├── gongrab-release.jks       # Android Release Keystore
│   └── launch4j.tgz              # Windows Packager Asset
│
├── native-apps/                   # 📱 Native Kotlin Multiplatform & Android TV Apps
│   ├── build.gradle.kts           # Root Gradle build script for native apps
│   ├── settings.gradle.kts        # Root Gradle settings (includes :composeApp, :androidTv)
│   ├── gradle.properties          # Gradle configuration properties
│   ├── local.properties           # Android SDK path configuration
│   ├── gradle/                    # Gradle wrapper distribution files
│   ├── composeApp/                # Jetpack Compose Multiplatform Desktop & Mobile App
│   └── androidTv/                 # Android TV Native App (Compose for TV)
│
├── server.js                      # 🚀 Express API Server (REST API on port 3000)
├── package.json                   # Node.js Package Manifest & Scripts
├── PROJECT_CONTEXT.md             # Master Vectorless RAG Index Document
├── .github/                       # GitHub Actions CI/CD (Deploys web/ to GitHub Pages)
└── .agents/                       # Custom AI Agent Skills (.agents/skills/project_context/)
```

---

## 3. Core Modules Breakdown

### A. Web Sub-Applications (`web/admin/` & `web/tv/`)
- **Web Admin Manager**: [`web/admin/index.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/index.html), [`web/admin/app.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/app.js), [`web/admin/styles.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/styles.css)
- **Digital TV Display**: [`web/tv/tv.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.html), [`web/tv/tv.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.js), [`web/tv/tv.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.css)
- **URL Redirect Helpers**: `web/index.html` and `web/tv.html` ensure legacy URLs forward smoothly to their target subfolders.

### B. Express API & Firebase Backend (`server.js`)
- **Files**: [`server.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/server.js), `package.json`, `config/grabngo-b5778-firebase-adminsdk-*.json`, `data/data_cache.json`
- **Role**: Express API server supplying REST endpoints for branch, category, and menu item management. Uses Firebase Admin SDK with offline fallback to `data/data_cache.json`.

### C. Native Kotlin Multiplatform & Android TV Apps (`native-apps/`)
- **Subdirectories**: `native-apps/composeApp`, `native-apps/androidTv`
- **Build Files**: `native-apps/build.gradle.kts`, `native-apps/settings.gradle.kts`, `native-apps/gradle.properties`, `native-apps/local.properties`, `native-apps/gradle/`

---

## 4. File-by-File Index & Function Map

| File | Lines | Purpose | Key Functions / Endpoints | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| [`server.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/server.js) | 402 | Express REST API server with Firebase Admin + Local JSON cache | `GET /api/branches`, `POST /api/branches`, `GET /api/categories`, `POST /api/categories`, `DELETE /api/categories/:id`, `GET /api/items`, `GET /api/branches/:id/menu`, `POST /api/items`, `PUT /api/items/:id`, `PATCH /api/items/:id/branch-status`, `DELETE /api/items/:id` | `express`, `cors`, `firebase-admin`, `data/data_cache.json` |
| [`web/admin/index.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/index.html) | 156 | Admin Dashboard HTML template | Controls, stats cards, branch/category filter tabs, menu table, add/edit item modals | `web/admin/styles.css`, `web/admin/app.js`, `FontAwesome` |
| [`web/admin/app.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/app.js) | 438 | Admin Dashboard logic | `loadData()`, `renderStats()`, `renderBranchTabs()`, `renderCategories()`, `renderItemsTable()`, `saveItem()`, `toggleBranchStatus()`, `saveBranch()` | REST API `/api/*` |
| [`web/admin/styles.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/styles.css) | 480 | Admin Dashboard glassmorphism dark mode stylesheet | Responsive grid, custom CSS variables (`--bg-primary`, `--accent-color`), modals, tables | None |
| [`web/tv/tv.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.html) | 23 | TV Menu Board HTML entry | `<main id="tv-board">`, Firebase Web SDK imports | `web/tv/tv.css`, `web/tv/tv.js`, Firebase Web SDK compat |
| [`web/tv/tv.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.js) | 670 | TV Menu Board live sync & display logic | `showBranchSelectionScreen()`, `fetchAndRenderBranches()`, `startLiveMenuListener()`, `renderTVBoard()`, `balanceColumns()`, `toggleFullscreen()` | Firebase Firestore SDK / `/api/branches/:id/menu` |
| [`web/tv/tv.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.css) | 275 | TV Board high-contrast display stylesheet | 5-column grid, branch selection modal overlay, D-pad focus indicators | None |
| [`native-apps/build.gradle.kts`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/native-apps/build.gradle.kts) | 11 | Root Gradle build script for native apps | Applies KMP & Android plugins | Gradle |
| [`native-apps/settings.gradle.kts`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/native-apps/settings.gradle.kts) | 22 | Root Gradle settings | Includes `:composeApp` and `:androidTv` | Gradle |
| [`scripts/seed.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/seed.js) | 167 | Excel seeder | Reads `/tmp/menu_sheet.xlsx` into Firestore. | `config/grabngo-b5778-*.json` |
| [`scripts/clean_seed.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/clean_seed.js) | 174 | Database wiper | Wipes and re-seeds Firestore collections. | `config/grabngo-b5778-*.json` |
| [`scripts/export_cache.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/export_cache.js) | 106 | Cache exporter | Writes live database to `data/data_cache.json`. | `data/data_cache.json` |
| [`scripts/verify.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/verify.js) | 75 | Integriy verifier | Validates Firestore collections and document counts. | `config/grabngo-b5778-*.json` |
| [`scripts/copy_data.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/copy_data.js) | 14 | Cache synchronizer | Syncs `data_cache.json` to static `data.json`. | `data/data_cache.json` |
| [`scripts/update_project_context.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/update_project_context.js) | 64 | Context index auditor | Scans project files and verifies file locations/line counts. | `PROJECT_CONTEXT.md` |

---

## 5. How AI Agents Should Use This Context Index (Vectorless RAG)

1. **Before Searching Code**: Search `PROJECT_CONTEXT.md` first to locate exact files, API routes, or database schemas.
2. **For Admin Web Dashboard Changes**: Inspect `web/admin/`.
3. **For TV Menu Board Display Changes**: Inspect `web/tv/`.
4. **For Native App / Gradle Changes**: Inspect `native-apps/`.
5. **For Backend & Cache Changes**: Inspect `server.js`, `config/`, and `data/`.
6. **For Code Audits**: Run `node scripts/update_project_context.js`.
