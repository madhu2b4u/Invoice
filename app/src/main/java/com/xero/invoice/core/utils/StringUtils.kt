package com.xero.invoice.core.utils

object StringUtils {

    private const val DEFAULT_SHORT_ID_LENGTH = 8

    /**
     * Generates a short ID from a full ID string
     * @param id Full ID string
     * @param length Desired length of short ID (default: 8)
     * @return Shortened ID string
     */
    fun generateShortId(id: String, length: Int = DEFAULT_SHORT_ID_LENGTH): String {
        return if (id.length <= length) {
            id
        } else {
            id.take(length)
        }
    }
}