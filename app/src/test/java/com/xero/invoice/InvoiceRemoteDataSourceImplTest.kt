package com.xero.invoice

import com.google.gson.Gson
import com.xero.invoice.core.exception.NoDataException
import com.xero.invoice.features.invoicecalculator.data.InvoiceApiService
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceItem
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceResponse
import com.xero.invoice.features.invoicecalculator.data.models.Invoices
import com.xero.invoice.features.invoicecalculator.data.source.InvoiceRemoteDataSourceImpl
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.io.InputStreamReader

class InvoiceRemoteDataSourceImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var service: InvoiceApiService
    private lateinit var invoiceRemoteDataSource: InvoiceRemoteDataSourceImpl
    private val gson = Gson()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mockk()
        invoiceRemoteDataSource = InvoiceRemoteDataSourceImpl(service, testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // Helper function to load JSON from resources
    private fun loadJson(filename: String): String {
        val inputStream = javaClass.classLoader!!.getResourceAsStream("raw/$filename")
            ?: throw IllegalArgumentException("File not found: $filename")
        return InputStreamReader(inputStream).use { it.readText() }
    }

    // Helper function to create a Retrofit Response
    private fun <T> createResponse(body: T?, isSuccessful: Boolean = true): Response<T> {
        return if (isSuccessful) {
            Response.success(body)
        } else {
            Response.error(400, "Error".toResponseBody("application/json".toMediaTypeOrNull()))
        }
    }

    // Helper function to create sample invoice data
    private fun createSampleInvoiceResponse(): InvoiceResponse {
        val lineItems = listOf(
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

        val invoice = Invoices(
            id = "f143404a-3e6c-4a61-98d0-5e9c3fe81d80",
            date = "2022-10-01T10:22:32",
            description = "Consulting services",
            items = lineItems
        )

        return InvoiceResponse(items = listOf(invoice))
    }

    @Test
    fun `getInvoices returns successful response with valid data`() = runTest {
        // Given
        val invoiceResponse = createSampleInvoiceResponse()
        val response = Response.success(invoiceResponse)
        coEvery { service.fetchInvoices() } returns CompletableDeferred(response)

        // When
        val result = invoiceRemoteDataSource.getInvoices()

        // Then
        assertEquals(invoiceResponse, result)
        assertEquals(1, result.items.size)
        assertEquals("f143404a-3e6c-4a61-98d0-5e9c3fe81d80", result.items[0].id)
        assertEquals("Consulting services", result.items[0].description)
        assertEquals(2, result.items[0].items.size)
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices returns InvoiceResponse from JSON file`() = runTest {
        // Arrange
        val invoicesJson = loadJson("invoices.json")
        val invoiceResponse = gson.fromJson(invoicesJson, InvoiceResponse::class.java)
        val response = createResponse(invoiceResponse)
        coEvery { service.fetchInvoices() } returns CompletableDeferred(response)

        // Act
        val result = invoiceRemoteDataSource.getInvoices()

        // Assert
        assertEquals(invoiceResponse, result)
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices throws HttpException on HTTP error response`() = runTest {
        // Given
        val errorResponse = CompletableDeferred(
            Response.error<InvoiceResponse>(404, "Not Found".toResponseBody(null))
        )
        coEvery { service.fetchInvoices() } returns errorResponse

        // When & Then
        try {
            invoiceRemoteDataSource.getInvoices()
            fail("Expected HttpException but no exception was thrown")
        } catch (exception: HttpException) {
            assertEquals(404, exception.code())
        }
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices throws NoDataException when response body is null`() = runTest {
        // Given
        val response = Response.success<InvoiceResponse>(null)
        val deferred = CompletableDeferred(response)
        coEvery { service.fetchInvoices() } returns deferred

        // When & Then
        try {
            invoiceRemoteDataSource.getInvoices()
            fail("Expected NoDataException but no exception was thrown")
        } catch (exception: NoDataException) {
            assertEquals("Response body is null", exception.message)
        }
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices throws IOException on network failure`() = runTest {
        // Given
        coEvery { service.fetchInvoices() } throws IOException("Network Failure")

        // When & Then
        try {
            invoiceRemoteDataSource.getInvoices()
            fail("Expected IOException but no exception was thrown")
        } catch (exception: IOException) {
            assertEquals("Network error occurred: Network Failure", exception.message)
        }
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices returns empty response successfully`() = runTest {
        // Given
        val emptyInvoiceResponse = InvoiceResponse(items = emptyList())
        val response = Response.success(emptyInvoiceResponse)
        coEvery { service.fetchInvoices() } returns CompletableDeferred(response)

        // When
        val result = invoiceRemoteDataSource.getInvoices()

        // Then
        assertEquals(emptyInvoiceResponse, result)
        assertEquals(0, result.items.size)
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices handles invoice with null description`() = runTest {
        // Given
        val invoiceWithNullDescription = Invoices(
            id = "test-id",
            date = "2022-10-01T10:22:32",
            description = null,
            items = listOf(
                InvoiceItem(
                    id = "item-id",
                    name = "Test Service",
                    quantity = 1,
                    priceinCents = 1000
                )
            )
        )
        val invoiceResponse = InvoiceResponse(items = listOf(invoiceWithNullDescription))
        val response = Response.success(invoiceResponse)
        coEvery { service.fetchInvoices() } returns CompletableDeferred(response)

        // When
        val result = invoiceRemoteDataSource.getInvoices()

        // Then
        assertEquals(invoiceResponse, result)
        assertEquals(null, result.items[0].description)
        assertEquals(1, result.items[0].items.size)
        coVerify(exactly = 1) { service.fetchInvoices() }
    }

    @Test
    fun `getInvoices handles server error correctly`() = runTest {
        // Given
        val errorResponse = CompletableDeferred(
            Response.error<InvoiceResponse>(500, "Internal Server Error".toResponseBody(null))
        )
        coEvery { service.fetchInvoices() } returns errorResponse

        // When & Then
        try {
            invoiceRemoteDataSource.getInvoices()
            fail("Expected HttpException but no exception was thrown")
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
        }
        coVerify(exactly = 1) { service.fetchInvoices() }
    }
}