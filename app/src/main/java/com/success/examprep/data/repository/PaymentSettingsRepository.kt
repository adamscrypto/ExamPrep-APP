package com.success.examprep.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.success.examprep.data.model.PaymentSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface PaymentSettingsRepository {
    fun getPaymentSettingsFlow(): Flow<PaymentSettings>
    suspend fun getPaymentSettings(): PaymentSettings
    suspend fun refreshPaymentSettings(): PaymentSettings
    suspend fun updatePaymentSettings(settings: PaymentSettings): Result<PaymentSettings>
    fun getCachedPaymentSettings(): PaymentSettings
}

class DefaultPaymentSettingsRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PaymentSettingsRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("examprep_payment_prefs", Context.MODE_PRIVATE)
    }

    private val _paymentSettingsFlow = MutableStateFlow(loadFromPrefs())

    init {
        // Prime cache initially
        _paymentSettingsFlow.value = loadFromPrefs()
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun getPaymentSettingsFlow(): Flow<PaymentSettings> = _paymentSettingsFlow.asStateFlow()

    override fun getCachedPaymentSettings(): PaymentSettings = _paymentSettingsFlow.value

    override suspend fun getPaymentSettings(): PaymentSettings = withContext(ioDispatcher) {
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val doc = firestore.collection("config").document("payment_settings").get().await()
                if (doc.exists() && doc.data != null) {
                    val settings = PaymentSettings.fromMap(doc.data!!, isLive = true)
                    saveToPrefs(settings)
                    _paymentSettingsFlow.value = settings
                    return@withContext settings
                }
            } catch (_: Exception) {
                // Network or permissions fallback to local cache
            }
        }
        val cached = loadFromPrefs().copy(isLiveConfig = firestore != null)
        _paymentSettingsFlow.value = cached
        cached
    }

    override suspend fun refreshPaymentSettings(): PaymentSettings = getPaymentSettings()

    override suspend fun updatePaymentSettings(settings: PaymentSettings): Result<PaymentSettings> = withContext(ioDispatcher) {
        val validation = settings.validate()
        if (validation.isFailure) {
            return@withContext Result.failure(validation.exceptionOrNull() ?: IllegalArgumentException("Invalid settings"))
        }

        var isLive = false
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                firestore.collection("config")
                    .document("payment_settings")
                    .set(settings.toMap(), SetOptions.merge())
                    .await()
                isLive = true
            } catch (_: Exception) {
                isLive = false
            }
        }

        val updatedSettings = settings.copy(
            updatedAtMillis = System.currentTimeMillis(),
            isLiveConfig = isLive || (firestore == null)
        )

        // Save to local cache & update state flow
        saveToPrefs(updatedSettings)
        _paymentSettingsFlow.value = updatedSettings
        Result.success(updatedSettings)
    }

    private fun loadFromPrefs(): PaymentSettings {
        val amount = prefs.getLong("amountNaira", 2500L)
        val bank = prefs.getString("bankName", "Zenith Bank") ?: "Zenith Bank"
        val accName = prefs.getString("accountName", "ExamPrep CBT Educational Services") ?: "ExamPrep CBT Educational Services"
        val accNum = prefs.getString("accountNumber", "1012345678") ?: "1012345678"
        val waNum = prefs.getString("whatsappNumber", "+2348012345678") ?: "+2348012345678"
        val instructions = prefs.getString(
            "paymentInstructions",
            "Transfer the exact fee to the bank account above. Tap 'Request Access via WhatsApp' to send your payment receipt and student details to the administrator. Access will be confirmed and granted manually."
        ) ?: "Transfer the exact fee to the bank account above. Tap 'Request Access via WhatsApp' to send your payment receipt and student details to the administrator. Access will be confirmed and granted manually."
        val updated = prefs.getLong("updatedAtMillis", 0L)

        return PaymentSettings(
            amountNaira = amount,
            bankName = bank,
            accountName = accName,
            accountNumber = accNum,
            whatsappNumber = waNum,
            paymentInstructions = instructions,
            isLiveConfig = true,
            updatedAtMillis = if (updated > 0) updated else System.currentTimeMillis()
        )
    }

    private fun saveToPrefs(settings: PaymentSettings) {
        prefs.edit()
            .putLong("amountNaira", settings.amountNaira)
            .putString("bankName", settings.bankName)
            .putString("accountName", settings.accountName)
            .putString("accountNumber", settings.accountNumber)
            .putString("whatsappNumber", settings.whatsappNumber)
            .putString("paymentInstructions", settings.paymentInstructions)
            .putLong("updatedAtMillis", settings.updatedAtMillis)
            .apply()
    }

    companion object {
        @Volatile
        private var instance: DefaultPaymentSettingsRepository? = null

        fun getInstance(context: Context): DefaultPaymentSettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultPaymentSettingsRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
