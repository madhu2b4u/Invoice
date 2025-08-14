package com.xero.invoice.features.invoicecalculator.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xero.invoice.R
import com.xero.invoice.features.invoicecalculator.presentation.viewmodel.InvoiceUiState

@Composable
fun InvoiceListScreen(
    uiState: InvoiceUiState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val invoices = uiState.invoices

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.invoices),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(
            count = invoices.size,
            key = { index -> invoices[index].invoice.id }
        ) { index ->
            val invoiceItem = invoices[index]
            InvoiceCard(invoiceItem = invoiceItem)
        }

        item {
            GrandTotalCard(
                grandTotal = uiState.grandTotal,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Empty state within the lazy column if no invoices but not in loading state
        if (invoices.isEmpty() && !uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_invoices_found),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}