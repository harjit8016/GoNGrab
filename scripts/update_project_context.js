const fs = require('fs');
const path = require('path');

const ROOT_DIR = path.join(__dirname, '..');
const CONTEXT_FILE = path.join(ROOT_DIR, 'PROJECT_CONTEXT.md');

function getLineCount(filePath) {
  try {
    if (!fs.existsSync(filePath)) return null;
    const content = fs.readFileSync(filePath, 'utf8');
    return content.split('\n').length;
  } catch (e) {
    return null;
  }
}

function scanProjectFiles() {
  console.log('=== Project Context & Vectorless RAG File Audit ===\n');

  const filesToTrack = [
    'server.js',
    'web/admin/index.html',
    'web/admin/app.js',
    'web/admin/styles.css',
    'web/tv/tv.html',
    'web/tv/tv.js',
    'web/tv/tv.css',
    'package.json',
    'data/data.json',
    'data/data_cache.json',
    'config/grabngo-b5778-firebase-adminsdk-fbsvc-ffc7ab1f34.json',
    'scripts/seed.js',
    'scripts/clean_seed.js',
    'scripts/export_cache.js',
    'scripts/verify.js',
    'scripts/copy_data.js',
    'scripts/update_project_context.js',
    'build-releases/GoNGrabMenuManagement-1.0.0.dmg',
    'native-apps/build.gradle.kts',
    'native-apps/settings.gradle.kts'
  ];

  const report = [];

  filesToTrack.forEach(relPath => {
    const absPath = path.join(ROOT_DIR, relPath);
    const lines = getLineCount(absPath);
    if (lines !== null) {
      report.push({ file: relPath, exists: true, lines });
      console.log(`✓ ${relPath.padEnd(25)} (${lines} lines)`);
    } else {
      report.push({ file: relPath, exists: false, lines: 0 });
      console.log(`✗ ${relPath.padEnd(25)} (NOT FOUND - May have been moved during restructuring)`);
    }
  });

  console.log('\nAudit completed.');
  return report;
}

if (require.main === module) {
  scanProjectFiles();
}
