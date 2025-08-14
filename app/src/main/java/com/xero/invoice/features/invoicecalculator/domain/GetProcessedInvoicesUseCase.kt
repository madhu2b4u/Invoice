package com.xero.invoice.features.invoicecalculator.domain

import com.xero.invoice.core.Result
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import kotlinx.coroutines.flow.Flow

interface GetProcessedInvoicesUseCase {
    suspend fun execute(): Flow<Result<ProcessedInvoiceData>>
}