package com.xero.invoice.features.invoicecalculator.data.repository

import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import com.xero.invoice.features.invoicecalculator.data.source.InvoiceRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import com.xero.invoice.core.Result
import com.xero.invoice.core.exception.NoDataException


class InvoiceRepositoryImpl @Inject constructor(
    private val remoteDataSource: InvoiceRemoteDataSource,
) : InvoiceRepository {

    override suspend fun getInvoices(): Flow<Result<InvoiceResponse>> = flow {
        emit(Result.loading())
        try {
            val invoices = remoteDataSource.getInvoices()
            emit(Result.success(invoices))
        } catch (e: IOException) {
            emit(Result.error("Network error: ${e.message}", null))
        } catch (e: NoDataException) {
            emit(Result.error(e.message ?: "No data found", null))
        } catch (e: Exception) {
            emit(Result.error(e.message ?: "Unknown error", null))
        }
    }
}