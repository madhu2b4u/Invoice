package com.xero.invoice.features.invoicecalculator.data.models


data class ProcessedInvoiceData(
    val invoices: List<InvoiceDisplayItem>,
    val grandTotalCents: Int,
    val grandTotalFormatted: String,
    val isEmpty: Boolean,
    val emptyMessage: String?
)

/**
 * Display data class for individual invoice items
 */
data class InvoiceDisplayItem(
    val invoice: Invoices,
    val totalInCents: Int,
    val formattedTotal: String,
    val formattedDate: String,
    val shortId: String
)
