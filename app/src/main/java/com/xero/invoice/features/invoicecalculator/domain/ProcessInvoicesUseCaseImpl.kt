package com.xero.invoice.features.invoicecalculator.domain


import com.xero.invoice.core.utils.CurrencyUtils
import com.xero.invoice.core.utils.DateUtils
import com.xero.invoice.core.utils.InvoiceConstants
import com.xero.invoice.core.utils.StringUtils
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceDisplayItem
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessInvoicesUseCaseImpl @Inject constructor() : ProcessInvoicesUseCase {

    override suspend fun execute(invoices: List<Invoices>): ProcessedInvoiceData {
        if (invoices.isEmpty()) {
            return createEmptyProcessedData()
        }

        val processedInvoices = processInvoices(invoices)
        val grandTotal = calculateGrandTotal(invoices)

        return ProcessedInvoiceData(
            invoices = processedInvoices,
            grandTotalCents = grandTotal,
            grandTotalFormatted = CurrencyUtils.formatCurrency(grandTotal),
            isEmpty = false,
            emptyMessage = null
        )
    }

    override fun processInvoices(invoices: List<Invoices>): List<InvoiceDisplayItem> {
        return invoices.map { invoice ->
            val total = calculateInvoiceTotal(invoice)
            InvoiceDisplayItem(
                invoice = invoice,
                totalInCents = total,
                formattedTotal = CurrencyUtils.formatCurrency(total),
                formattedDate = DateUtils.formatInvoiceDate(invoice.date),
                shortId = StringUtils.generateShortId(invoice.id)
            )
        }
    }

    override fun calculateInvoiceTotal(invoice: Invoices): Int {
        return invoice.items.sumOf { lineItem ->
            validateAndCalculateLineItemTotal(lineItem.quantity, lineItem.priceinCents)
        }
    }

    override fun calculateGrandTotal(invoices: List<Invoices>): Int {
        return invoices.sumOf { calculateInvoiceTotal(it) }
    }

    private fun createEmptyProcessedData(): ProcessedInvoiceData {
        return ProcessedInvoiceData(
            invoices = emptyList(),
            grandTotalCents = 0,
            grandTotalFormatted = CurrencyUtils.formatCurrency(0),
            isEmpty = true,
            emptyMessage = InvoiceConstants.NO_INVOICES_MESSAGE
        )
    }

    private fun validateAndCalculateLineItemTotal(quantity: Int, priceInCents: Int): Int {
        val validQuantity = maxOf(quantity, InvoiceConstants.MIN_QUANTITY)
        val validPrice = maxOf(priceInCents, InvoiceConstants.MIN_PRICE_CENTS)

        return validQuantity * validPrice
    }
}