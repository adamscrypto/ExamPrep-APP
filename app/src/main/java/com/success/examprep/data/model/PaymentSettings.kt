package com.success.examprep.data.model

/**
 * Authoritative administrator-configurable payment and WhatsApp settings model.
 * Initial default fee is ₦2,500 with Zenith Bank as the primary bank details.
 * Editable by authorized administrators from the Admin Dashboard and persisted in Firestore `config/payment_settings`.
 */
data class PaymentSettings(
    val amountNaira: Long = 2500L,
    val bankName: String = "Zenith Bank",
    val accountName: String = "ExamPrep CBT Educational Services",
    val accountNumber: String = "1012345678",
    val whatsappNumber: String = "+2348012345678",
    val paymentInstructions: String = "Transfer the exact fee to the bank account above. Tap 'Request Access via WhatsApp' to send your payment receipt and student details to the administrator. Access will be confirmed and granted manually.",
    val isLiveConfig: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val formattedAmount: String
        get() = "₦%,d".format(amountNaira)

    val cleanWhatsAppNumber: String
        get() = whatsappNumber.replace("+", "").replace(" ", "").replace("-", "").trim()

    /**
     * Validates that payment settings adhere to administrative criteria:
     * - Positive non-zero amount in Naira
     * - Non-blank bank name, account name, and account number
     * - Valid WhatsApp business phone number with country code
     */
    fun validate(): Result<Unit> {
        if (amountNaira <= 0) {
            return Result.failure(IllegalArgumentException("Payment amount must be greater than ₦0."))
        }
        if (bankName.isBlank()) {
            return Result.failure(IllegalArgumentException("Bank name cannot be empty."))
        }
        if (accountName.isBlank()) {
            return Result.failure(IllegalArgumentException("Account name cannot be empty."))
        }
        if (accountNumber.isBlank() || accountNumber.trim().length < 5) {
            return Result.failure(IllegalArgumentException("Account number must be at least 5 digits."))
        }
        val cleanNumber = cleanWhatsAppNumber
        if (cleanNumber.isBlank() || cleanNumber.length < 10 || !cleanNumber.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Please enter a valid WhatsApp number with country code (e.g., +2348012345678)."))
        }
        return Result.success(Unit)
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "amountNaira" to amountNaira,
            "bankName" to bankName.trim(),
            "accountName" to accountName.trim(),
            "accountNumber" to accountNumber.trim(),
            "whatsappNumber" to whatsappNumber.trim(),
            "paymentInstructions" to paymentInstructions.trim(),
            "updatedAtMillis" to updatedAtMillis
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, isLive: Boolean = true): PaymentSettings {
            val amount = (map["amountNaira"] as? Number)?.toLong() ?: 2500L
            val bank = (map["bankName"] as? String)?.ifBlank { "Zenith Bank" } ?: "Zenith Bank"
            val accName = (map["accountName"] as? String)?.ifBlank { "ExamPrep CBT Educational Services" } ?: "ExamPrep CBT Educational Services"
            val accNum = (map["accountNumber"] as? String)?.ifBlank { "1012345678" } ?: "1012345678"
            val waNum = (map["whatsappNumber"] as? String)?.ifBlank { "+2348012345678" } ?: "+2348012345678"
            val instructions = (map["paymentInstructions"] as? String)?.ifBlank {
                "Transfer the exact fee to the bank account above. Tap 'Request Access via WhatsApp' to send your payment receipt and student details to the administrator. Access will be confirmed and granted manually."
            } ?: "Transfer the exact fee to the bank account above. Tap 'Request Access via WhatsApp' to send your payment receipt and student details to the administrator. Access will be confirmed and granted manually."
            val updated = (map["updatedAtMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()

            return PaymentSettings(
                amountNaira = amount,
                bankName = bank,
                accountName = accName,
                accountNumber = accNum,
                whatsappNumber = waNum,
                paymentInstructions = instructions,
                isLiveConfig = isLive,
                updatedAtMillis = updated
            )
        }
    }
}
