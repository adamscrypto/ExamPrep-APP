# Firestore Schema Specification & Data Architecture

This document defines the schema, collection contracts, data types, and security boundaries for **ExamPrep CBT** on Cloud Firestore.

---

## 1. System Architecture & Entity Relationships

```text
               Firebase Authentication
                          │
                          ▼ (provides UID)
                     users/{uid}
                          │
       ┌──────────────────┼──────────────────┐
       ▼                  ▼                  ▼
  entitlements        questions          cbtHistory
{entitlementId}      {questionId}       {historyId}
 (Subject Grant)     (CBT Bank)        (Test Session)
       │                  │                  │
       └──────────────┬───┴──────────────────┘
                      ▼
               CBT Exam Session
             (First 20 Questions Free;
           Questions 21+ Locked Without
                Active Entitlement)
```

---

## 2. Collections Specification

### 2.1 Collection: `users`
- **Path:** `/users/{uid}`
- **Document ID:** Must exactly match the Firebase Authentication UID (`request.auth.uid`). Clients cannot specify an arbitrary UID.
- **Purpose:** Stores student user profiles and preferences.

| Field | Type | Required | Description |
|---|---|---|---|
| `uid` | String | Yes | Unique identifier matching Firebase Auth UID. |
| `fullName` | String | Yes | Student's full name. |
| `email` | String | Yes | Registered email address. |
| `phone` | String / Null | No | Contact phone number. |
| `profilePhotoUrl` | String / Null | No | Profile photo URL. |
| `accountStatus` | String | Yes | Account status: `"ACTIVE"`, `"SUSPENDED"`. |
| `targetExam` | String | No | Target examination (e.g., `"JAMB"`, `"WAEC"`, `"NECO"`). |
| `targetYear` | Number | No | Planned examination year. |
| `createdAt` | Timestamp | Yes | Creation timestamp (`FieldValue.serverTimestamp()`). |
| `updatedAt` | Timestamp | Yes | Last update timestamp. |

> **Security Invariant:** Passwords are NEVER stored, transmitted, or accepted in Firestore. Security rules reject any write containing `password` or `confirmPassword`.

#### Permissions:
- **Read:** `isOwner(uid) || isAdmin()`
- **Create:** `isOwner(uid) && request.resource.data.uid == uid && noPassword()`
- **Update:** `isOwner(uid) && request.resource.data.uid == uid && noPassword()`
- **Delete:** `isAdmin()`

---

### 2.2 Collection: `questions`
- **Path:** `/questions/{questionId}`
- **Document ID:** Numeric or alphanumeric question identifier (e.g., `"1001"`, `"2004"`).
- **Purpose:** Centralized examination question bank for JAMB, WAEC, and NECO exams.

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | Number | Yes | Numeric question ID. |
| `examType` | String | Yes | Examination identifier: `"jamb"`, `"waec"`, `"neco"`. |
| `subject` | String | Yes | Subject identifier: `"mathematics"`, `"english"`, `"physics"`, `"chemistry"`, `"biology"`, `"economics"`, etc. |
| `year` | Number | Yes | Examination year (1990–2030). |
| `topic` | String | Yes | Curriculum syllabus topic. |
| `questionText` | String | Yes | Full question prompt/stem text. |
| `optionA` | String | Yes | Option A text. |
| `optionB` | String | Yes | Option B text. |
| `optionC` | String | Yes | Option C text. |
| `optionD` | String | Yes | Option D text. |
| `correctAnswer` | String | Yes | Correct option letter: `"A"`, `"B"`, `"C"`, or `"D"`. |
| `explanation` | String | No | Detailed solution or explanation. |
| `difficulty` | String | Yes | Difficulty rating: `"EASY"`, `"MEDIUM"`, `"HARD"`. |
| `isFree` | Boolean | Yes | Baseline flag (CBT engine enforces 20-free rule). |
| `isActive` | Boolean | Yes | Question visibility toggle. Inactive questions are hidden from students. |
| `createdAt` | Number / Timestamp | Yes | Timestamp of creation. |
| `createdBy` | String | Yes | Firebase Auth UID of the administrator who created or imported the question. |
| `updatedAt` | Number / Timestamp | Yes | Timestamp of last modification. |
| `updatedBy` | String | Yes | Firebase Auth UID of the administrator who modified the question. |

#### Permissions:
- **Read:** Active questions (`isActive == true`) are readable by all authenticated students/guests for CBT practice. Inactive questions are readable only by admins (`isAdmin()`).
- **Create / Update / Delete:** Strictly restricted to administrators (`request.auth.token.admin == true`).

---

### 2.3 Collection: `entitlements`
- **Path:** `/entitlements/{entitlementId}`
- **Document ID:** Unique entitlement ID (e.g., `"ent_3a4b5c6d7e8f"`).
- **Purpose:** Manages specific access rights granted to students for individual exams and subjects beyond the first 20 free questions.

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Unique entitlement document ID. |
| `studentUid` | String | Yes | Firebase Auth UID of the entitled student. |
| `studentEmail` | String | Yes | Student's email for administrative audit and search. |
| `studentName` | String | Yes | Student's display name. |
| `examType` | String | Yes | Scoped exam type (e.g., `"JAMB"`). |
| `subjectType` | String | Yes | Scoped subject type (e.g., `"MATHEMATICS"`). |
| `accessType` | String | Yes | Access tier: `"MANUAL_ADMIN"`, `"PURCHASED"`, `"PROMOTIONAL"`. |
| `status` | String | Yes | Entitlement state: `"ACTIVE"`, `"EXPIRED"`, `"REVOKED"`. |
| `grantedAt` | Number | Yes | Epoch millis when entitlement was issued. |
| `grantedBy` | String | Yes | Administrator UID or name who authorized the grant. |
| `expiresAt` | Number / Null | No | Expiration timestamp in epoch millis (`null` = permanent). |
| `reason` | String | No | Administrative reason for grant or revocation. |
| `createdAt` | Number | Yes | Record creation epoch millis. |
| `updatedAt` | Number | Yes | Record update epoch millis. |

#### Permissions:
- **Read:** Student can read their own entitlements (`resource.data.studentUid == request.auth.uid`) or administrator (`isAdmin()`).
- **Create / Update / Delete:** Strictly restricted to administrators (`request.auth.token.admin == true`). Students cannot grant access to themselves.

---

### 2.4 Collection: `cbtHistory`
- **Path:** `/cbtHistory/{historyId}`
- **Document ID:** Unique session record identifier (e.g., `"test_7b8c9d0e"`).
- **Purpose:** Records student CBT examination results, performance statistics, and timing history.

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Unique session history ID. |
| `studentUid` | String | Yes | Firebase Auth UID of the student who took the test. |
| `examType` | String | Yes | Exam taken (e.g., `"JAMB"`). |
| `subjectId` | String | Yes | Subject identifier (e.g., `"mathematics"`). |
| `subjectName` | String | Yes | Subject display name. |
| `scorePercentage` | Number | Yes | Score percentage (0–100). |
| `correctCount` | Number | Yes | Number of correct answers. |
| `wrongCount` | Number | Yes | Number of wrong answers. |
| `unansweredCount`| Number | Yes | Number of skipped/unanswered questions. |
| `totalQuestions` | Number | Yes | Total questions presented in session. |
| `timeSpentSeconds`| Number | Yes | Time taken in seconds. |
| `timestampMillis` | Number | Yes | Epoch timestamp of completion. |
| `isFreePracticeMode` | Boolean | Yes | `true` if first 20 free questions session; `false` if full unlocked session. |
| `createdAt` | Timestamp | Yes | Server timestamp (`FieldValue.serverTimestamp()`). |

#### Permissions:
- **Read:** Student can read their own history (`resource.data.studentUid == request.auth.uid`) or administrator (`isAdmin()`).
- **Create:** Authenticated student for their own UID (`request.resource.data.studentUid == request.auth.uid`).
- **Update / Delete:** Student owner or administrator.
