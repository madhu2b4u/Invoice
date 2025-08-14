package com.xero.invoice.features.invoicecalculator.di

import com.xero.invoice.features.invoicecalculator.data.InvoiceApiService
import com.xero.invoice.features.invoicecalculator.data.source.InvoiceRemoteDataSource
import com.xero.invoice.features.invoicecalculator.data.source.InvoiceRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module(includes = [InvoiceRemoteModule.Binders::class])
@InstallIn(SingletonComponent::class)
class InvoiceRemoteModule {
    @Module
    @InstallIn(SingletonComponent::class)
    interface Binders {

        @Binds
        fun bindsRemoteSource(
            remoteDataSourceImpl: InvoiceRemoteDataSourceImpl
        ): InvoiceRemoteDataSource
    }


    @Provides
    fun provideService(retrofit: Retrofit): InvoiceApiService =
        retrofit.create(InvoiceApiService::class.java)
}