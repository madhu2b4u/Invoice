package com.xero.invoice.features.invoicecalculator.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.xero.invoice.features.invoicecalculator.presentation.viewmodel.InvoiceUiState
import com.xero.invoice.features.invoicecalculator.presentation.viewmodel.InvoiceViewModel
import kotlinx.coroutines.launch

@Composable
fun MainInvoiceScreen(
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show snack-bar for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                runCatching {
                    snackBarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Short
                    )
                }.onSuccess {
                    viewModel.clearError()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        InvoiceContent(
            uiState = uiState,
            listState = listState
        )

        // Snackbar Host
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun InvoiceContent(
    uiState: InvoiceUiState,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                LoadingScreen()
            }

            uiState.isEmpty -> {
                uiState.emptyMessage?.let { EmptyScreen(message = it) }
            }

            else -> {
                InvoiceListScreen(
                    uiState = uiState,
                    listState = listState
                )
            }
        }
    }
}