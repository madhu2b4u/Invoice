package com.xero.invoice.features.invoicecalculator.domain

import com.xero.invoice.features.invoicecalculator.data.models.InvoiceDisplayItem
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData


interface ProcessInvoicesUseCase {
    suspend fun execute(invoices: List<Invoices>): ProcessedInvoiceData
    fun processInvoices(invoices: List<Invoices>): List<InvoiceDisplayItem>
    fun calculateInvoiceTotal(invoice: Invoices): Int
    fun calculateGrandTotal(invoices: List<Invoices>): Int
}