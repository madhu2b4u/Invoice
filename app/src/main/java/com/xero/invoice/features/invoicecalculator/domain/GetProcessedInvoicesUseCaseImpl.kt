package com.xero.invoice.features.invoicecalculator.domain


import com.xero.invoice.core.Result
import com.xero.invoice.core.utils.CurrencyUtils
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GetProcessedInvoicesUseCaseImpl @Inject constructor(
    private val getInvoicesUseCase: InvoiceUseCase,
    private val processInvoicesUseCase: ProcessInvoicesUseCase
) : GetProcessedInvoicesUseCase {

    override suspend fun execute(): Flow<Result<ProcessedInvoiceData>> {
        return getInvoicesUseCase.getInvoices().map { result ->
            when (result) {
                is Result.Loading -> Result.loading()
                is Result.Success -> handleSuccessResult(result.data)
                is Result.Empty -> handleEmptyResult(result.title, result.message)
                is Result.Error -> Result.error(result.message)
            }
        }
    }

    private suspend fun handleSuccessResult(response: InvoiceResponse): Result<ProcessedInvoiceData> {
        return try {
            val processedData = processInvoicesUseCase.execute(response.items)
            Result.success(processedData)
        } catch (e: Exception) {
            Result.error("Failed to process invoices: ${e.message}")
        }
    }

    private suspend fun handleEmptyResult(
        title: String,
        message: String
    ): Result<ProcessedInvoiceData> {
        val emptyData = ProcessedInvoiceData(
            invoices = emptyList(),
            grandTotalCents = 0,
            grandTotalFormatted = CurrencyUtils.formatCurrency(0),
            isEmpty = true,
            emptyMessage = message
        )
        return Result.success(emptyData)
    }
}