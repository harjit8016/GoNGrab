---
name: project_context
description: Use and maintain the master project context index (PROJECT_CONTEXT.md) for instant vectorless RAG contextual retrieval and codebase mapping.
---

# Project Context & Vectorless RAG Skill

## Purpose
This skill provides guidelines and instructions for inspecting, querying, and updating the repository's master context file [`PROJECT_CONTEXT.md`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/PROJECT_CONTEXT.md). 

Using `PROJECT_CONTEXT.md` allows any AI agent turn to quickly locate files, understanding data models, API endpoints, and component responsibilities without needing to re-scan or grep through the entire repository.

---

## Key Modules Quick Reference

- **Web Admin Dashboard**: [`web/admin/index.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/index.html), [`web/admin/app.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/app.js), [`web/admin/styles.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/admin/styles.css)
- **TV Menu Board Display**: [`web/tv/tv.html`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.html), [`web/tv/tv.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.js), [`web/tv/tv.css`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/web/tv/tv.css)
- **Node.js Express Server**: [`server.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/server.js)
- **Data Caches & Credentials**: [`data/data_cache.json`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/data/data_cache.json), [`data/data.json`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/data/data.json), `config/grabngo-b5778-*.json`
- **Packaged Releases**: `build-releases/`
- **Native Apps & Gradle**: `native-apps/`
- **Scripts**: [`scripts/seed.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/seed.js), [`scripts/export_cache.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/export_cache.js), [`scripts/clean_seed.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/clean_seed.js), [`scripts/verify.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/verify.js), [`scripts/copy_data.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/copy_data.js), [`scripts/update_project_context.js`](file:///Users/harjitsingh/Documents/expgravity/Restaurant%20menu/scripts/update_project_context.js)

---

## Maintenance Guidelines

When updating `PROJECT_CONTEXT.md`:
- Run `node scripts/update_project_context.js` after structural file changes.
- Maintain clear formatting with markdown tables.
- Include file links using standard `file:///` URLs.
