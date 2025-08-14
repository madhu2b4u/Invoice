package com.xero.invoice.features.invoicecalculator.data.source

import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse


interface InvoiceRemoteDataSource {

    suspend fun getInvoices(): InvoiceResponse

}