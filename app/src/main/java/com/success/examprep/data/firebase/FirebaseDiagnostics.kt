package com.success.examprep.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Diagnostic report representation for internal Firebase connection verification.
 * Does not expose API keys or sensitive credentials.
 */
data class FirebaseDiagnosticReport(
    val isInitialized: Boolean,
    val projectId: String,
    val registeredPackageName: String,
    val isAuthAvailable: Boolean,
    val currentUserId: String?,
    val currentUserEmail: String?,
    val isAdminClaim: Boolean,
    val isFirestoreReachable: Boolean,
    val firestoreLatencyMs: Long,
    val details: String
)

object FirebaseDiagnostics {

    suspend fun runDiagnostic(context: Context): FirebaseDiagnosticReport = withContext(Dispatchers.IO) {
        var isInit = false
        var projId = "Not initialized"
        var pkgName = context.packageName
        var authAvailable = false
        var uid: String? = null
        var email: String? = null
        var adminClaim = false
        var firestoreOk = false
        var latencyMs = -1L

        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                val app = apps.first()
                isInit = true
                projId = app.options.projectId ?: "Unknown"
            }
        } catch (_: Exception) {}

        if (isInit) {
            try {
                val auth = FirebaseAuth.getInstance()
                authAvailable = true
                val user = auth.currentUser
                if (user != null) {
                    uid = user.uid
                    email = user.email
                    val token = user.getIdToken(false).await()
                    adminClaim = token.claims["admin"] == true || token.claims["admin"] == "true"
                }
            } catch (_: Exception) {}

            try {
                val startTime = System.currentTimeMillis()
                val firestore = FirebaseFirestore.getInstance()
                // Ping Firestore by reading questions collection metadata or a single test doc
                firestore.collection("questions").limit(1).get().await()
                firestoreOk = true
                latencyMs = System.currentTimeMillis() - startTime
            } catch (_: Exception) {
                firestoreOk = false
            }
        }

        val details = buildString {
            appendLine("Firebase Initialization: ${if (isInit) "Active" else "Inactive"}")
            appendLine("Target Project ID: $projId")
            appendLine("App Package Name: $pkgName")
            appendLine("Firebase Auth: ${if (authAvailable) "Ready" else "Unavailable"}")
            appendLine("Session: ${email ?: "Guest / Signed Out"}")
            if (uid != null) {
                appendLine("UID: ${uid.take(6)}...${uid.takeLast(4)}")
                appendLine("Admin Claim: ${if (adminClaim) "Verified (admin: true)" else "Standard Student"}")
            }
            appendLine("Firestore Connectivity: ${if (firestoreOk) "Online (${latencyMs}ms)" else "Offline / Local Mode"}")
        }

        FirebaseDiagnosticReport(
            isInitialized = isInit,
            projectId = projId,
            registeredPackageName = pkgName,
            isAuthAvailable = authAvailable,
            currentUserId = uid,
            currentUserEmail = email,
            isAdminClaim = adminClaim,
            isFirestoreReachable = firestoreOk,
            firestoreLatencyMs = latencyMs,
            details = details
        )
    }
}
