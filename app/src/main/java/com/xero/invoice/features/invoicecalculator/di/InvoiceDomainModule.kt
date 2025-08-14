package com.xero.invoice.features.invoicecalculator.di

import com.xero.invoice.features.invoicecalculator.data.repository.InvoiceRepository
import com.xero.invoice.features.invoicecalculator.data.repository.InvoiceRepositoryImpl
import com.xero.invoice.features.invoicecalculator.domain.GetProcessedInvoicesUseCase
import com.xero.invoice.features.invoicecalculator.domain.GetProcessedInvoicesUseCaseImpl
import com.xero.invoice.features.invoicecalculator.domain.InvoiceUseCase
import com.xero.invoice.features.invoicecalculator.domain.InvoiceUseCaseImpl
import com.xero.invoice.features.invoicecalculator.domain.ProcessInvoicesUseCase
import com.xero.invoice.features.invoicecalculator.domain.ProcessInvoicesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InvoiceDomainModule {

    @Binds
    internal abstract fun bindRepository(
        repoImpl: InvoiceRepositoryImpl
    ): InvoiceRepository


    @Binds
    internal abstract fun bindInvoiceUseCase(
        useCaseImpl: InvoiceUseCaseImpl
    ): InvoiceUseCase

    @Binds
    internal abstract fun bindProcessInvoicesUseCase(
        useCaseImpl: ProcessInvoicesUseCaseImpl
    ): ProcessInvoicesUseCase

    @Binds
    internal abstract fun bindGetProcessedInvoicesUseCase(
        useCaseImpl: GetProcessedInvoicesUseCaseImpl
    ): GetProcessedInvoicesUseCase
}