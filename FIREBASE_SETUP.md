# Firebase Setup & Deployment Guide for ExamPrep CBT

This document provides complete instructions for connecting the **ExamPrep CBT** Android application to your Firebase backend.

---

## 1. Project Configuration Summary

| Property | Value |
|---|---|
| **Android Application ID** | `com.success.examprep` |
| **Android Namespace** | `com.success.examprep` |
| **Firebase Project ID** | `examprep-ff363` |
| **Configuration File** | `app/google-services.json` (detected and verified) |
| **Auth Provider** | Email / Password |
| **Database** | Cloud Firestore |

---

## 2. Status Overview

### ✅ Already Completed
1. **Firebase Project Created:** `examprep-ff363` in Google Firebase Console.
2. **Android App Registered:** Package registered as `com.success.examprep`.
3. **Configuration Downloaded:** `app/google-services.json` placed in the `app/` directory and verified.
4. **Initial Admin Account Created:** Admin user registered in Firebase Authentication.
5. **Client Architecture Configured:**
   - Real Firebase Auth (`FirebaseAuthRepository`) with persistent session.
   - Cloud Firestore data repositories for `users`, `questions`, `entitlements`, and `cbtHistory`.
   - Complete offline fallback to local storage so CBT practice and admin tools never crash if connectivity drops.
   - Stage 6 Bulk Question Import (Excel `.xlsx` and CSV `.csv`) connected to Firestore batch writes.
   - Standardized canonical package `com.success.examprep` across the entire codebase.

### ⏳ Still Required (Console & CLI Actions)
Follow the step-by-step instructions below to finalize your backend.

---

## 3. Firebase Console Actions (One-Time Setup)

### Step 1: Enable Email/Password Authentication
1. Go to the [Firebase Console](https://console.firebase.google.com/project/examprep-ff363/authentication/providers).
2. Navigate to **Build** > **Authentication** > **Sign-in method**.
3. Click **Email/Password**.
4. Toggle **Enable** to ON (leave "Email link (passwordless sign-in)" OFF).
5. Click **Save**.

### Step 2: Create Cloud Firestore Database
1. In the left sidebar, navigate to **Build** > **Firestore Database**.
2. Click **Create database**.
3. Choose a Firestore location close to your students (e.g., `europe-west2` or `europe-west1`).
4. Select **Start in production mode** (our security rules will be deployed in the next step).
5. Click **Create**.
*Note: Firestore is schemaless. Collections (`users`, `questions`, `entitlements`, `cbtHistory`) will be generated automatically when documents are created.*

---

## 4. Firebase CLI Deployment (Security Rules & Indexes)

You can deploy the security rules and indexes directly from this repository using the Firebase CLI.

### Prerequisites
Install the Firebase CLI if you haven't already:
```bash
npm install -g firebase-tools
```

### Deployment Commands
From the root of this project:

```bash
# 1. Log into your Google account
firebase login

# 2. Select the active project
firebase use examprep-ff363

# 3. Deploy Firestore Security Rules
firebase deploy --only firestore:rules

# 4. Deploy Firestore Composite Indexes
firebase deploy --only firestore:indexes
```

Or deploy both at once:
```bash
firebase deploy --only firestore
```

---

## 5. Setting the Admin Custom Claim (`admin: true`)

ExamPrep CBT secures admin endpoints using **Firebase Authentication Custom Claims** (`admin: true`). Creating a user in Firebase Auth does not make them an administrator until this custom claim is set.

> **Security Warning:** The Firebase Admin private key must **NEVER** be bundled into the Android APK. Assigning custom claims must be performed server-side or via administrative CLI scripts.

### Method A: Using the Provided Node.js Admin Script

1. Download a service account private key from Firebase Console:
   - Go to **Project Settings** > **Service accounts**.
   - Click **Generate new private key** and save it as `serviceAccountKey.json` in your local directory (outside version control).

2. Run the provided script:
```bash
node scripts/set-admin-claim.js serviceAccountKey.json <ADMIN_USER_EMAIL_OR_UID>
```

### Method B: Using Google Cloud Shell / Firebase Functions / Node REPL
```javascript
const admin = require('firebase-admin');
admin.initializeApp();

// Replace with your admin's UID
const adminUid = "YOUR_ADMIN_AUTH_UID";

admin.auth().setCustomUserClaims(adminUid, { admin: true })
  .then(() => {
    console.log(`Successfully granted admin claim to UID: ${adminUid}`);
  })
  .catch(error => {
    console.error('Error setting custom claim:', error);
  });
```

*Note: Once set, the admin user should sign out and sign back in on the Android app to refresh their ID token.*

---

## 6. Verification and Diagnostics

The Android application includes an in-app Firebase Diagnostic tool accessible to administrators:
- Checks Firebase initialization status.
- Confirms project ID matches `examprep-ff363`.
- Verifies ID token claims (`admin: true`).
- Pings Firestore connectivity and measures response latency.
- Validates offline fallback readiness.
