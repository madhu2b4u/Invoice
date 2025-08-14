package com.xero.invoice.features.invoicecalculator.data.repository

import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import kotlinx.coroutines.flow.Flow
import com.xero.invoice.core.Result

interface InvoiceRepository {

    suspend fun getInvoices(): Flow<Result<InvoiceResponse>>
}