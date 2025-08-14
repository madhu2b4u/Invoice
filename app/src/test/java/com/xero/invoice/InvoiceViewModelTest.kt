package com.xero.invoice

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.xero.invoice.core.Result
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceDisplayItem
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceItem
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import com.xero.invoice.features.invoicecalculator.domain.GetProcessedInvoicesUseCase
import com.xero.invoice.features.invoicecalculator.presentation.viewmodel.InvoiceViewModel
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class InvoiceViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val sampleInvoices = listOf(
        Invoices(
            id = "invoice-1",
            date = "2022-10-01T10:22:32",
            description = "Consulting services",
            items = listOf(
                InvoiceItem("item1", "Service #1", 2, 1000),
                InvoiceItem("item2", "Service #2", 1, 500)
            )
        )
    )

    private val sampleDisplayItems = listOf(
        InvoiceDisplayItem(
            invoice = sampleInvoices[0],
            totalInCents = 2500,
            formattedTotal = "$25.00",
            formattedDate = "Oct 01, 2022",
            shortId = "invoice-1"
        )
    )

    private val sampleProcessedData = ProcessedInvoiceData(
        invoices = sampleDisplayItems,
        grandTotalCents = 2500,
        grandTotalFormatted = "$25.00",
        isEmpty = false,
        emptyMessage = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }


    @Test
    fun `viewModel initializes and calls use case`() = runTest {
        // Given
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.success(sampleProcessedData))

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { mockUseCase.execute() }
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.invoices.size)
        assertEquals("$25.00", uiState.grandTotal)
    }

    @Test
    fun `success state updates UI correctly`() = runTest {
        // Given
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.success(sampleProcessedData))

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isEmpty)
        assertEquals("invoice-1", uiState.invoices[0].invoice.id)
        assertEquals("Consulting services", uiState.invoices[0].invoice.description)
        assertEquals(2500, uiState.invoices[0].totalInCents)
        assertNull(uiState.error)
    }

    @Test
    fun `error state handles network failure`() = runTest {
        // Given
        val errorMessage = "Network connection failed"
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.error(errorMessage))

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
        assertTrue(uiState.invoices.isEmpty())
        assertEquals("$0.00", uiState.grandTotal)
    }

    @Test
    fun `empty state shows when no invoices available`() = runTest {
        // Given
        val emptyData = ProcessedInvoiceData(
            invoices = emptyList(),
            grandTotalCents = 0,
            grandTotalFormatted = "$0.00",
            isEmpty = true,
            emptyMessage = "No invoices found"
        )
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.success(emptyData))

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isEmpty)
        assertTrue(uiState.invoices.isEmpty())
        assertEquals("$0.00", uiState.grandTotal)
        assertEquals("No invoices found", uiState.emptyMessage)
    }

    @Test
    fun `clearError removes error state`() = runTest {
        // Given
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.error("Test error"))
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertEquals("Test error", viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loading then success state transition`() = runTest {
        // Given
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(
            Result.loading(),
            Result.success(sampleProcessedData)
        )

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading) // Final state should not be loading
        assertEquals(1, uiState.invoices.size)
        assertEquals("$25.00", uiState.grandTotal)
        assertNull(uiState.error)
        coVerify(exactly = 1) { mockUseCase.execute() }
    }

    @Test
    fun `handles null description in invoice data`() = runTest {
        // Given
        val invoiceWithNullDesc = Invoices(
            id = "null-desc-invoice",
            date = "2022-10-01T10:22:32",
            description = null, // Null description
            items = listOf(InvoiceItem("item1", "Service", 1, 1000))
        )
        val displayItem = InvoiceDisplayItem(
            invoice = invoiceWithNullDesc,
            totalInCents = 1000,
            formattedTotal = "$10.00",
            formattedDate = "Oct 01, 2022",
            shortId = "null-des"
        )
        val dataWithNull = ProcessedInvoiceData(
            invoices = listOf(displayItem),
            grandTotalCents = 1000,
            grandTotalFormatted = "$10.00",
            isEmpty = false,
            emptyMessage = null
        )
        val mockUseCase = mockk<GetProcessedInvoicesUseCase>()
        coEvery { mockUseCase.execute() } returns flowOf(Result.success(dataWithNull))

        // When
        val viewModel = InvoiceViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.invoices.size)
        assertNull(uiState.invoices[0].invoice.description)
        assertEquals("$10.00", uiState.grandTotal)
        assertFalse(uiState.isEmpty)
    }
}