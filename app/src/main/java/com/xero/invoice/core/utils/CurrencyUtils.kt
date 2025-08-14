package com.xero.invoice.core.utils

import java.text.DecimalFormat

object CurrencyUtils {

    private const val CENTS_TO_DOLLARS = 100.0
    const val CURRENCY_PATTERN = "$#,##0.00"

    // Cache formatter for performance
    private val currencyFormatter = DecimalFormat(CURRENCY_PATTERN)

    /**
     * Formats cents to currency string
     * @param cents Amount in cents
     * @return Formatted currency string (e.g., "$12.34")
     */
    fun formatCurrency(cents: Int): String {
        val dollars = cents / CENTS_TO_DOLLARS
        return currencyFormatter.format(dollars)
    }
}

