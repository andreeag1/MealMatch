import admin from 'firebase-admin';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { readFileSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const serviceAccount = JSON.parse(
  readFileSync(join(__dirname, '../../firebase-service-account.json'))
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: 'meal-match-1.firebasestorage.app'
});

export default admin;