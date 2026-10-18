/**
 * Script to assign the 'admin: true' custom claim to a Firebase Authentication user.
 * 
 * Usage:
 *   node scripts/set-admin-claim.js <path-to-service-account-key.json> <user-email-or-uid>
 * 
 * Example:
 *   node scripts/set-admin-claim.js serviceAccountKey.json admin@examprep.cbt
 */

const admin = require('firebase-admin');
const fs = require('fs');

const args = process.argv.slice(2);
if (args.length < 2) {
  console.error("Usage: node scripts/set-admin-claim.js <serviceAccountKeyPath> <emailOrUid>");
  process.exit(1);
}

const [keyPath, userIdentifier] = args;

if (!fs.existsSync(keyPath)) {
  console.error(`Error: Service account file not found at ${keyPath}`);
  process.exit(1);
}

const serviceAccount = JSON.parse(fs.readFileSync(keyPath, 'utf8'));

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function setAdminClaim() {
  try {
    let user;
    if (userIdentifier.includes('@')) {
      user = await admin.auth().getUserByEmail(userIdentifier);
    } else {
      user = await admin.auth().getUser(userIdentifier);
    }

    console.log(`Found user: ${user.email} (UID: ${user.uid})`);
    
    // Set admin custom claim
    await admin.auth().setCustomUserClaims(user.uid, { admin: true });
    
    console.log(`\nSUCCESS: Custom claim { admin: true } granted to ${user.email}.`);
    console.log("Note: The user should sign out and sign back in to refresh their ID token.");
    process.exit(0);
  } catch (error) {
    console.error("Failed to set admin custom claim:", error.message);
    process.exit(1);
  }
}

setAdminClaim();
