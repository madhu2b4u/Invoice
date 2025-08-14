package com.xero.invoice.features.invoicecalculator.data.models

data class InvoiceResponse(
    val items: List<Invoices>
)

data class Invoices(
    val date: String,
    val id: String,
    val description: String?,
    val items: List<InvoiceItem>
)

data class InvoiceItem(
    val id: String,
    val name: String,
    val priceinCents: Int,
    val quantity: Int
)