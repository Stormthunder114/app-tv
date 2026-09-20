package com.example.util

import java.util.Locale

object PixPayloadGenerator {
    const val PIX_KEY = "42920009293"
    const val MERCHANT_NAME = "ASSIST PLUS"
    const val MERCHANT_CITY = "SAO PAULO"

    /**
     * Generates a genuine BR Code (Pix EMV) static payload based on the official
     * Banco Central do Brasil Pix specifications.
     */
    fun generatePixCode(
        pixKey: String = PIX_KEY,
        amount: Double? = null,
        merchantName: String = MERCHANT_NAME,
        merchantCity: String = MERCHANT_CITY,
        txId: String = "***"
    ): String {
        val cleanKey = pixKey.trim()
        val cleanName = merchantName.trim().take(25).uppercase()
        val cleanCity = merchantCity.trim().take(15).uppercase()
        val cleanTxId = if (txId.isBlank()) "***" else txId.trim().take(25)

        val sb = StringBuilder()
        // 00: Payload Format Indicator
        formatField(sb, "00", "01")
        // 01: Point of Initiation Method (11 = Static QR)
        formatField(sb, "01", "11")

        // 26: Merchant Account Information
        val merchantAccount = StringBuilder()
        formatField(merchantAccount, "00", "br.gov.bcb.pix")
        formatField(merchantAccount, "01", cleanKey)
        formatField(sb, "26", merchantAccount.toString())

        // 52: Merchant Category Code
        formatField(sb, "52", "0000")
        // 53: Transaction Currency (986 = BRL)
        formatField(sb, "53", "986")

        // 54: Transaction Amount
        if (amount != null && amount > 0.0) {
            val formattedAmount = "%.2f".format(Locale.US, amount)
            formatField(sb, "54", formattedAmount)
        }

        // 58: Country Code
        formatField(sb, "58", "BR")
        // 59: Merchant Name
        formatField(sb, "59", if (cleanName.isNotEmpty()) cleanName else "ASSIST PLUS")
        // 60: Merchant City
        formatField(sb, "60", if (cleanCity.isNotEmpty()) cleanCity else "SAO PAULO")

        // 62: Additional Data Field (TxID)
        val additionalData = StringBuilder()
        formatField(additionalData, "05", cleanTxId)
        formatField(sb, "62", additionalData.toString())

        // 63: CRC16
        sb.append("6304")
        val crc = calculateCrc16(sb.toString())
        sb.append(crc)

        return sb.toString()
    }

    private fun formatField(sb: StringBuilder, id: String, value: String) {
        val lenStr = "%02d".format(value.length)
        sb.append(id).append(lenStr).append(value)
    }

    fun calculateCrc16(payload: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021
        val bytes = payload.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = ((b.toInt() shr (7 - i)) and 1) == 1
                val c15 = ((crc shr 15) and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor polynomial
            }
        }
        crc = crc and 0xFFFF
        return "%04X".format(crc)
    }
}
