const fs = require('fs');
const { execSync } = require('child_process');
try {
  const out = execSync('npx ng test --watch=false', { encoding: 'utf-8' });
  fs.writeFileSync('out_readable.txt', 'SUCCESS:\n' + out);
} catch (e) {
  let errOut = 'STDOUT:\n' + (e.stdout || '') + '\nSTDERR:\n' + (e.stderr || '');
  fs.writeFileSync('out_readable.txt', errOut);
}
