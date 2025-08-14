package com.xero.invoice

import com.xero.invoice.core.Result
import com.xero.invoice.core.exception.NoDataException
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceItem
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.repository.InvoiceRepositoryImpl
import com.xero.invoice.features.invoicecalculator.data.source.InvoiceRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class InvoiceRepositoryImplTest {

    private lateinit var repository: InvoiceRepositoryImpl
    private lateinit var remoteDataSource: InvoiceRemoteDataSource
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val sampleLineItems = listOf(
        InvoiceItem(
            id = "c100f400-0da3-4161-8f16-dba7d0b4356a",
            name = "Service #1",
            quantity = 1,
            priceinCents = 100
        ),
        InvoiceItem(
            id = "8149d691-c303-48e2-a8b3-8976d7e8489a",
            name = "Service #2",
            quantity = 2,
            priceinCents = 750
        )
    )

    private val sampleInvoices = listOf(
        Invoices(
            id = "f143404a-3e6c-4a61-98d0-5e9c3fe81d80",
            date = "2022-10-01T10:22:32",
            description = "Consulting services for Q3",
            items = sampleLineItems
        )
    )

    private val sampleInvoiceResponse = InvoiceResponse(items = sampleInvoices)

    @Before
    fun setup() {
        remoteDataSource = mockk()
        repository = InvoiceRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getInvoices returns success when remote data source succeeds`() = testScope.runTest {
        // Given
        coEvery { remoteDataSource.getInvoices() } returns sampleInvoiceResponse

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Success)

        val successResult = result[1] as Result.Success
        assertEquals(sampleInvoiceResponse, successResult.data)
        assertEquals(1, successResult.data.items.size)
        assertEquals("f143404a-3e6c-4a61-98d0-5e9c3fe81d80", successResult.data.items[0].id)
        assertEquals("Consulting services for Q3", successResult.data.items[0].description)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices returns success with empty invoice list`() = testScope.runTest {
        // Given
        val emptyInvoiceResponse = InvoiceResponse(items = emptyList())
        coEvery { remoteDataSource.getInvoices() } returns emptyInvoiceResponse

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Success)

        val successResult = result[1] as Result.Success
        assertEquals(emptyInvoiceResponse, successResult.data)
        assertEquals(0, successResult.data.items.size)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices handles IOException from remote source`() = testScope.runTest {
        // Given
        val networkError = IOException("Network connection failed")
        coEvery { remoteDataSource.getInvoices() } throws networkError

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Error)

        val errorResult = result[1] as Result.Error
        assertEquals("Network error: Network connection failed", errorResult.message)
        assertNull(errorResult.data)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices handles NoDataException`() = testScope.runTest {
        // Given
        val noDataError = NoDataException("No invoices found on server")
        coEvery { remoteDataSource.getInvoices() } throws noDataError

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Error)

        val errorResult = result[1] as Result.Error
        assertEquals("No invoices found on server", errorResult.message)
        assertNull(errorResult.data)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices handles generic exception with null message`() = testScope.runTest {
        // Given
        val genericError = RuntimeException(null as String?)
        coEvery { remoteDataSource.getInvoices() } throws genericError

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Error)

        val errorResult = result[1] as Result.Error
        assertEquals("Unknown error", errorResult.message)
        assertNull(errorResult.data)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices handles invoice with null description`() = testScope.runTest {
        // Given
        val invoiceWithNullDescription = Invoices(
            id = "null-desc-id",
            date = "2022-10-01T10:22:32",
            description = null,
            items = sampleLineItems
        )
        val invoiceResponse = InvoiceResponse(items = listOf(invoiceWithNullDescription))
        coEvery { remoteDataSource.getInvoices() } returns invoiceResponse

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0] is Result.Loading)
        assertTrue(result[1] is Result.Success)

        val successResult = result[1] as Result.Success
        assertNull(successResult.data.items[0].description)
        assertEquals("null-desc-id", successResult.data.items[0].id)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }

    @Test
    fun `getInvoices maintains proper flow state sequence`() = testScope.runTest {
        // Given
        coEvery { remoteDataSource.getInvoices() } returns sampleInvoiceResponse

        // When
        val result = repository.getInvoices().toList()

        // Then
        assertEquals(2, result.size)
        assertTrue("First emission should be Loading", result[0] is Result.Loading)
        assertTrue("Second emission should be Success", result[1] is Result.Success)

        // Verify data integrity is maintained
        val successResult = result[1] as Result.Success
        val returnedInvoice = successResult.data.items[0]
        assertEquals("f143404a-3e6c-4a61-98d0-5e9c3fe81d80", returnedInvoice.id)
        assertEquals(2, returnedInvoice.items.size)

        coVerify(exactly = 1) { remoteDataSource.getInvoices() }
    }
}