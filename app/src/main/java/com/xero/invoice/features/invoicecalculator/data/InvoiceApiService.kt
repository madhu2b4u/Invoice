package com.xero.invoice.features.invoicecalculator.data

import com.xero.invoice.core.ApiConstants.INVOICE_ENDPOINT
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface InvoiceApiService {
    @GET(INVOICE_ENDPOINT)
    fun fetchInvoices(): Deferred<Response<InvoiceResponse>>
}