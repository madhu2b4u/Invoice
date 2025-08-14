package com.xero.invoice.features.invoicecalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xero.invoice.core.Result
import com.xero.invoice.core.utils.InvoiceConstants
import com.xero.invoice.features.invoicecalculator.data.models.InvoiceDisplayItem
import com.xero.invoice.features.invoicecalculator.data.models.ProcessedInvoiceData
import com.xero.invoice.features.invoicecalculator.domain.GetProcessedInvoicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getProcessedInvoicesUseCase: GetProcessedInvoicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    /**
     * Initiates invoice loading process
     */
    private fun loadInvoices() {
        viewModelScope.launch {
            getProcessedInvoicesUseCase.execute().collectLatest { result ->
                handleInvoiceResult(result)
            }
        }
    }

    /**
     * Clears the current error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Handles the result from the use case and updates UI state accordingly
     */
    private fun handleInvoiceResult(result: Result<ProcessedInvoiceData>) {
        when (result) {
            is Result.Loading -> setLoadingState()
            is Result.Success -> setSuccessState(result.data)
            is Result.Empty -> setEmptyState(result.title, result.message)
            is Result.Error -> setErrorState(result.message)
        }
    }

    /**
     * Sets the UI to loading state
     */
    private fun setLoadingState() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )
    }

    /**
     * Sets the UI to success state with processed data
     */
    private fun setSuccessState(data: ProcessedInvoiceData) {
        _uiState.value = InvoiceUiState(
            isLoading = false,
            isEmpty = data.isEmpty,
            invoices = data.invoices,
            grandTotal = data.grandTotalFormatted,
            error = null,
            emptyMessage = data.emptyMessage
        )
    }

    /**
     * Sets the UI to empty state
     */
    private fun setEmptyState(title: String, message: String) {
        _uiState.value = InvoiceUiState(
            isLoading = false,
            isEmpty = true,
            invoices = emptyList(),
            grandTotal = InvoiceConstants.DEFAULT_CURRENCY_VALUE,
            error = null,
            emptyMessage = message
        )
    }

    /**
     * Sets the UI to error state
     */
    private fun setErrorState(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }
}

/**
 * UI State for the Invoice List screen
 */
data class InvoiceUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val invoices: List<InvoiceDisplayItem> = emptyList(),
    val grandTotal: String = InvoiceConstants.DEFAULT_CURRENCY_VALUE,
    val error: String? = null,
    val emptyMessage: String? = null
)