package com.xero.invoice.core.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {

    private const val INPUT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
    private const val OUTPUT_DATE_PATTERN = "MMM dd, yyyy"

    // Cache formatters for performance
    private val inputDateFormatter = SimpleDateFormat(INPUT_DATE_PATTERN, Locale.getDefault())
    private val outputDateFormatter = SimpleDateFormat(OUTPUT_DATE_PATTERN, Locale.getDefault())

    /**
     * Formats ISO8601 date string to readable format
     * @param dateString ISO8601 date string (e.g., "2022-10-01T10:22:32")
     * @return Formatted date string (e.g., "Oct 01, 2022") or original string if parsing fails
     */
    fun formatInvoiceDate(dateString: String): String {
        return try {
            val date = inputDateFormatter.parse(dateString)
            date?.let { outputDateFormatter.format(it) } ?: dateString
        } catch (e: Exception) {
            // Log error for debugging
            android.util.Log.w("DateUtils", "Failed to parse date: $dateString", e)
            dateString // Fallback to original string
        }
    }
}