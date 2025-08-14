package com.xero.invoice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.xero.invoice.core.Result
import com.xero.invoice.core.utils.CurrencyUtils
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceDisplayItem
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceItem
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import com.xero.invoice.features.invoicecalculator.data.repository.InvoiceRepository
import com.xero.invoice.features.invoicecalculator.domain.GetProcessedInvoicesUseCaseImpl
import com.xero.invoice.features.invoicecalculator.domain.InvoiceUseCase
import com.xero.invoice.features.invoicecalculator.domain.InvoiceUseCaseImpl
import com.xero.invoice.features.invoicecalculator.domain.ProcessInvoicesUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceUseCaseImplTest {

    private lateinit var repository: InvoiceRepository
    private lateinit var useCase: InvoiceUseCaseImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        repository = mockk()
        useCase = InvoiceUseCaseImpl(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getInvoices returns success flow from repository`() = testScope.runTest {
        // Given
        val mockResponse = InvoiceResponse(items = emptyList())
        val mockResult = Result.success(mockResponse)
        coEvery { repository.getInvoices() } returns flowOf(mockResult)

        // When
        val result = useCase.getInvoices().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(mockResponse, (result[0] as Result.Success).data)
        coVerify(exactly = 1) { repository.getInvoices() }
    }

    @Test
    fun `getInvoices handles error state from repository`() = testScope.runTest {
        // Given
        val errorMessage = "Network error"
        val mockResult = Result.error<InvoiceResponse>(errorMessage)
        coEvery { repository.getInvoices() } returns flowOf(mockResult)

        // When
        val result = useCase.getInvoices().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(errorMessage, (result[0] as Result.Error).message)
        coVerify(exactly = 1) { repository.getInvoices() }
    }
}
