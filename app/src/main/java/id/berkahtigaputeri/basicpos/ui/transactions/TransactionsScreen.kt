package id.berkahtigaputeri.basicpos.ui.transactions

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import id.berkahtigaputeri.basicpos.core.money.Rupiah
import id.berkahtigaputeri.basicpos.core.money.formatIdr
import id.berkahtigaputeri.basicpos.domain.model.SaleReceipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val receipt by viewModel.receipt.collectAsState()
    val message by viewModel.message.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Riwayat transaksi") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                label = { Text("Cari nomor transaksi") }
            )

            if (message != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(message!!, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::dismissMessage) { Text("Tutup") }
                    }
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = if (query.isBlank()) {
                        "Belum ada transaksi yang tersimpan."
                    } else {
                        "Nomor transaksi tidak ditemukan."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { sale ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { viewModel.openReceipt(sale.id) }
                                .semantics {
                                    contentDescription = "Buka struk transaksi ${sale.transactionNumber}, total ${Rupiah.from(sale.totalRupiah).formatIdr()}"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(sale.transactionNumber, style = MaterialTheme.typography.titleSmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        formatTransactionDate(sale.createdAtEpochMillis),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        Rupiah.from(sale.totalRupiah).formatIdr(),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Text(
                                    text = "Dibayar ${Rupiah.from(sale.paidRupiah).formatIdr()}  •  Kembalian ${Rupiah.from(sale.changeRupiah).formatIdr()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    receipt?.let { saved ->
        AlertDialog(
            onDismissRequest = viewModel::closeReceipt,
            title = { Text("Struk transaksi") },
            text = { ReceiptDetails(saved) },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildReceiptText(saved))
                    }
                    context.startActivity(Intent.createChooser(intent, "Bagikan struk"))
                }) {
                    Text("Bagikan")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeReceipt) { Text("Tutup") }
            }
        )
    }
}

@Composable
private fun ReceiptDetails(receipt: SaleReceipt) {
    val storeProfile = receipt.storeProfileSnapshot
    Column(
        modifier = Modifier
            .heightIn(max = 480.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(storeProfile?.storeName ?: "BERKAH POS", style = MaterialTheme.typography.titleMedium)
        storeProfile?.ownerName?.takeIf(String::isNotBlank)?.let { Text("Pemilik: $it") }
        storeProfile?.address?.takeIf(String::isNotBlank)?.let { Text(it) }
        storeProfile?.phoneNumber?.takeIf(String::isNotBlank)?.let { Text(it) }
        Text(receipt.sale.transactionNumber, style = MaterialTheme.typography.labelLarge)
        Text(formatTransactionDate(receipt.sale.createdAtEpochMillis))
        Spacer(modifier = Modifier.height(4.dp))
        receipt.items.forEach { item ->
            Text("${item.productName} × ${item.quantity}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(Rupiah.from(item.unitPriceRupiah).formatIdr())
                Text(Rupiah.from(item.lineTotalRupiah).formatIdr())
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        ReceiptAmountRow("Subtotal", receipt.subtotalRupiah)
        ReceiptAmountRow("Diskon", receipt.discountRupiah)
        ReceiptAmountRow("Pajak", receipt.taxRupiah)
        ReceiptAmountRow("Total", receipt.sale.totalRupiah)
        ReceiptAmountRow("Dibayar", receipt.sale.paidRupiah)
        ReceiptAmountRow("Kembalian", receipt.sale.changeRupiah)
        storeProfile?.receiptFooter?.takeIf(String::isNotBlank)?.let { Text(it) }
    }
}

@Composable
private fun ReceiptAmountRow(label: String, amount: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(Rupiah.from(amount).formatIdr())
    }
}

private fun formatTransactionDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        .format(Date(epochMillis))

private fun buildReceiptText(receipt: SaleReceipt): String = buildString {
    val storeProfile = receipt.storeProfileSnapshot
    appendLine(storeProfile?.storeName ?: "BERKAH POS")
    storeProfile?.ownerName?.takeIf(String::isNotBlank)?.let { appendLine("Pemilik: $it") }
    storeProfile?.address?.takeIf(String::isNotBlank)?.let { appendLine(it) }
    storeProfile?.phoneNumber?.takeIf(String::isNotBlank)?.let { appendLine(it) }
    appendLine(receipt.sale.transactionNumber)
    appendLine(formatTransactionDate(receipt.sale.createdAtEpochMillis))
    appendLine("------------------------------")
    receipt.items.forEach { item ->
        appendLine(item.productName)
        appendLine("  ${item.quantity} x ${Rupiah.from(item.unitPriceRupiah).formatIdr()} = ${Rupiah.from(item.lineTotalRupiah).formatIdr()}")
    }
    appendLine("------------------------------")
    appendLine("Subtotal: ${Rupiah.from(receipt.subtotalRupiah).formatIdr()}")
    appendLine("Diskon: ${Rupiah.from(receipt.discountRupiah).formatIdr()}")
    appendLine("Pajak: ${Rupiah.from(receipt.taxRupiah).formatIdr()}")
    appendLine("Total: ${Rupiah.from(receipt.sale.totalRupiah).formatIdr()}")
    appendLine("Dibayar: ${Rupiah.from(receipt.sale.paidRupiah).formatIdr()}")
    appendLine("Kembalian: ${Rupiah.from(receipt.sale.changeRupiah).formatIdr()}")
    storeProfile?.receiptFooter?.takeIf(String::isNotBlank)?.let { appendLine(it) }
}
