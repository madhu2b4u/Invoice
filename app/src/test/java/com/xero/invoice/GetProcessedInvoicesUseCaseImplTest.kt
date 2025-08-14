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
class GetProcessedInvoicesUseCaseImplTest {

    private lateinit var getInvoicesUseCase: InvoiceUseCase
    private lateinit var processInvoicesUseCase: ProcessInvoicesUseCase
    private lateinit var useCase: GetProcessedInvoicesUseCaseImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val sampleInvoiceResponse = InvoiceResponse(
        items = listOf(
            Invoices(
                id = "invoice-1",
                date = "2022-10-01T10:22:32",
                description = "Test invoice",
                items = listOf(
                    InvoiceItem("item1", "Service 1", 1, 1000)
                )
            )
        )
    )

    private val sampleProcessedData = ProcessedInvoiceData(
        invoices = listOf(
            InvoiceDisplayItem(
                invoice = sampleInvoiceResponse.items[0],
                totalInCents = 1000,
                formattedTotal = "$10.00",
                formattedDate = "Oct 01, 2022",
                shortId = "invoice-1"
            )
        ),
        grandTotalCents = 1000,
        grandTotalFormatted = "$10.00",
        isEmpty = false,
        emptyMessage = null
    )

    @Before
    fun setUp() {
        getInvoicesUseCase = mockk()
        processInvoicesUseCase = mockk()
        useCase = GetProcessedInvoicesUseCaseImpl(getInvoicesUseCase, processInvoicesUseCase)

        // Mock utility objects
        mockkObject(CurrencyUtils)
        every { CurrencyUtils.formatCurrency(any()) } returns "$0.00"
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `execute processes success result correctly`() = testScope.runTest {
        // Given
        coEvery { getInvoicesUseCase.getInvoices() } returns flowOf(Result.success(sampleInvoiceResponse))
        coEvery { processInvoicesUseCase.execute(sampleInvoiceResponse.items) } returns sampleProcessedData

        // When
        val result = useCase.execute().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(sampleProcessedData, (result[0] as Result.Success).data)
        coVerify(exactly = 1) { getInvoicesUseCase.getInvoices() }
        coVerify(exactly = 1) { processInvoicesUseCase.execute(sampleInvoiceResponse.items) }
    }

    @Test
    fun `execute handles error result correctly`() = testScope.runTest {
        // Given
        val errorMessage = "Network error occurred"
        coEvery { getInvoicesUseCase.getInvoices() } returns flowOf(Result.error(errorMessage))

        // When
        val result = useCase.execute().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(errorMessage, (result[0] as Result.Error).message)
        coVerify(exactly = 1) { getInvoicesUseCase.getInvoices() }
        coVerify(exactly = 0) { processInvoicesUseCase.execute(any()) }
    }

    @Test
    fun `execute handles empty result correctly`() = testScope.runTest {
        // Given
        val emptyResult = Result.empty<InvoiceResponse>("No Data", "No invoices available")
        coEvery { getInvoicesUseCase.getInvoices() } returns flowOf(emptyResult)
        every { CurrencyUtils.formatCurrency(0) } returns "$0.00"

        // When
        val result = useCase.execute().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        val successResult = result[0] as Result.Success
        assertTrue(successResult.data.isEmpty)
        assertEquals("No invoices available", successResult.data.emptyMessage)
        assertEquals(0, successResult.data.grandTotalCents)
        coVerify(exactly = 1) { getInvoicesUseCase.getInvoices() }
    }

    @Test
    fun `execute handles processing exception`() = testScope.runTest {
        // Given
        val exception = RuntimeException("Processing failed")
        coEvery { getInvoicesUseCase.getInvoices() } returns flowOf(Result.success(sampleInvoiceResponse))
        coEvery { processInvoicesUseCase.execute(any()) } throws exception

        // When
        val result = useCase.execute().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals("Failed to process invoices: Processing failed", (result[0] as Result.Error).message)
        coVerify(exactly = 1) { getInvoicesUseCase.getInvoices() }
        coVerify(exactly = 1) { processInvoicesUseCase.execute(sampleInvoiceResponse.items) }
    }

    @Test
    fun `execute handles loading then success state transition`() = testScope.runTest {
        // Given
        val loadingResult = Result.loading<InvoiceResponse>()
        val successResult = Result.success(sampleInvoiceResponse)
        coEvery { getInvoicesUseCase.getInvoices() } returns flowOf(loadingResult, successResult)
        coEvery { processInvoicesUseCase.execute(any()) } returns sampleProcessedData

        // When
        val result = useCase.execute().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Success)
        assertEquals(sampleProcessedData, (result[1] as Result.Success).data)
        coVerify(exactly = 1) { getInvoicesUseCase.getInvoices() }
        coVerify(exactly = 1) { processInvoicesUseCase.execute(sampleInvoiceResponse.items) }
    }
}