const fs = require('fs');
const path = require('path');

const localCachePath = path.join(__dirname, 'data_cache.json');
const rootDataPath = path.join(__dirname, 'data.json');

if (fs.existsSync(localCachePath)) {
  const content = fs.readFileSync(localCachePath, 'utf8');
  fs.writeFileSync(rootDataPath, content);
  console.log('✓ Successfully created static data.json in root directory.');
} else {
  console.error('data_cache.json not found!');
}
