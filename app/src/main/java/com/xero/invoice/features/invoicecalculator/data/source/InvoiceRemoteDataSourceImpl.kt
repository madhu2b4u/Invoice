package com.xero.invoice.features.invoicecalculator.data.source

import com.xero.invoice.core.exception.NoDataException
import com.xero.invoice.core.qualifiers.IO
import com.xero.invoice.features.invoicecalculator.data.InvoiceApiService
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class InvoiceRemoteDataSourceImpl @Inject constructor(
    private val service: InvoiceApiService,
    @IO private val context: CoroutineContext
) : InvoiceRemoteDataSource {
    override suspend fun getInvoices() = withContext(context) {
        try {
            val response = service.fetchInvoices().await()
            if (response.isSuccessful) {
                response.body() ?: throw NoDataException("Response body is null")
            } else {
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw IOException("Network error occurred: ${e.message}", e)
        }
    }
}