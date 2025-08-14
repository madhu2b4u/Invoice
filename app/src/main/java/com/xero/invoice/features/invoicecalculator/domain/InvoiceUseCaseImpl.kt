package com.xero.invoice.features.invoicecalculator.domain

import com.xero.invoice.features.invoicecalculator.data.repository.InvoiceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceUseCaseImpl @Inject constructor(private val repository: InvoiceRepository) :
    InvoiceUseCase {
    override suspend fun getInvoices() = repository.getInvoices()
}
