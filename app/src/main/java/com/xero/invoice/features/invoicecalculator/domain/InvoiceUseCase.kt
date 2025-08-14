package com.xero.invoice.features.invoicecalculator.domain

import com.xero.invoice.core.Result
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import kotlinx.coroutines.flow.Flow

interface InvoiceUseCase {

    suspend fun getInvoices(): Flow<Result<InvoiceResponse>>
}