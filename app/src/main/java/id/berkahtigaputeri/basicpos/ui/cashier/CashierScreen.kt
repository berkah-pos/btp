package id.berkahtigaputeri.basicpos.ui.cashier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import id.berkahtigaputeri.basicpos.core.money.Rupiah
import id.berkahtigaputeri.basicpos.core.money.formatIdr
import id.berkahtigaputeri.basicpos.domain.model.Product

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: CashierViewModel
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val message by viewModel.message.collectAsState()
    val receipt by viewModel.receipt.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val cartHasCurrentStock = cart.all { it.product.stockQuantity >= it.quantity }

    var discountText by remember { mutableStateOf("0") }
    var taxText by remember { mutableStateOf("0") }
    var paidText by remember { mutableStateOf("") }

    val subtotal = remember(cart) {
        runCatching {
            cart.fold(0L) { total, item ->
                Math.addExact(total, Math.multiplyExact(item.product.priceRupiah, item.quantity))
            }
        }.getOrNull()
    }
    val discount = discountText.ifBlank { "0" }.toLongOrNull()
    val tax = taxText.ifBlank { "0" }.toLongOrNull()
    val total = remember(subtotal, discount, tax) {
        if (
            subtotal == null ||
            discount == null ||
            tax == null ||
            discount < 0 ||
            discount > subtotal ||
            tax < 0
        ) {
            null
        } else {
            runCatching {
                Math.addExact(Math.subtractExact(subtotal, discount), tax)
            }.getOrNull()
        }
    }
    val paid = paidText.toLongOrNull()
    val change = if (total != null && paid != null && paid >= total) paid - total else null

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kasir") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Pilih produk", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cari nama produk atau SKU") }
                )
            }

            if (products.isEmpty()) {
                item {
                    Text(
                        text = "Produk belum tersedia. Tambahkan produk terlebih dahulu pada menu Produk.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(products, key = { "product-${it.id}" }) { product ->
                    CashierProductCard(
                        product = product,
                        isSubmitting = isSubmitting,
                        onAdd = { viewModel.addProduct(product) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Keranjang", style = MaterialTheme.typography.titleLarge)
            }

            if (cart.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada item dalam keranjang.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(cart, key = { "cart-${it.product.id}" }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(item.product.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${Rupiah.from(item.product.priceRupiah).formatIdr()} × ${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { viewModel.changeQuantity(item.product.id, -1) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Kurangi jumlah ${item.product.name}"
                                    },
                                    enabled = !isSubmitting
                                ) { Text("−") }
                                Text(
                                    item.quantity.toString(),
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                TextButton(
                                    onClick = { viewModel.changeQuantity(item.product.id, 1) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Tambah jumlah ${item.product.name}"
                                    },
                                    enabled = !isSubmitting
                                ) { Text("+") }
                                TextButton(
                                    onClick = { viewModel.removeProduct(item.product.id) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Hapus ${item.product.name} dari keranjang"
                                    },
                                    enabled = !isSubmitting
                                ) { Text("Hapus") }
                            }
                            Text(
                                text = "Jumlah: ${runCatching {
                                    Rupiah.from(Math.multiplyExact(item.product.priceRupiah, item.quantity)).formatIdr()
                                }.getOrElse { "Nilai terlalu besar" }}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                Text("Pembayaran", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSubmitting,
                    label = { Text("Diskon (Rupiah, opsional)") }
                )
                OutlinedTextField(
                    value = taxText,
                    onValueChange = { taxText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSubmitting,
                    label = { Text("Pajak (Rupiah, opsional)") }
                )
                OutlinedTextField(
                    value = paidText,
                    onValueChange = { paidText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSubmitting,
                    label = { Text("Uang diterima") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                PaymentSummaryRow("Subtotal", subtotal?.let { Rupiah.from(it).formatIdr() } ?: "Nilai terlalu besar")
                PaymentSummaryRow("Diskon", discount?.let { Rupiah.from(it).formatIdr() } ?: "Nominal tidak valid")
                PaymentSummaryRow("Pajak", tax?.let { Rupiah.from(it).formatIdr() } ?: "Nominal tidak valid")
                PaymentSummaryRow("Total", total?.let { Rupiah.from(it).formatIdr() } ?: "Periksa diskon/pajak")
                PaymentSummaryRow("Kembalian", change?.let { Rupiah.from(it).formatIdr() } ?: "—")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (total != null && paid != null) {
                            viewModel.checkout(discount!!, tax!!, paid)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cart.isNotEmpty() &&
                        !isSubmitting &&
                        cartHasCurrentStock &&
                        subtotal != null &&
                        discount != null &&
                        tax != null &&
                        total != null &&
                        paid != null &&
                        paid >= total
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = "Menyimpan transaksi"
                            }
                        )
                    } else {
                        Text("Simpan transaksi")
                    }
                }
            }

            if (message != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(message!!, modifier = Modifier.weight(1f))
                            TextButton(onClick = viewModel::dismissMessage) { Text("Tutup") }
                        }
                    }
                }
            }
        }
    }

    receipt?.let { saved ->
        AlertDialog(
            onDismissRequest = viewModel::clearReceipt,
            title = { Text("Transaksi tersimpan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    val receiptProfile = saved.storeProfileSnapshot
                    Text(receiptProfile?.storeName ?: "BERKAH POS", style = MaterialTheme.typography.titleMedium)
                    receiptProfile?.address?.takeIf(String::isNotBlank)?.let { Text(it) }
                    receiptProfile?.phoneNumber?.takeIf(String::isNotBlank)?.let { Text(it) }
                    Text(saved.sale.transactionNumber)
                    Text("Total: ${Rupiah.from(saved.sale.totalRupiah).formatIdr()}")
                    Text("Dibayar: ${Rupiah.from(saved.sale.paidRupiah).formatIdr()}")
                    Text("Kembalian: ${Rupiah.from(saved.sale.changeRupiah).formatIdr()}")
                    Text("${saved.items.size} jenis produk")
                    receiptProfile?.receiptFooter?.takeIf(String::isNotBlank)?.let { Text(it) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearReceipt()
                    discountText = "0"
                    taxText = "0"
                    paidText = ""
                }) { Text("Selesai") }
            }
        )
    }
}

@Composable
private fun CashierProductCard(
    product: Product,
    isSubmitting: Boolean,
    onAdd: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text(Rupiah.from(product.priceRupiah).formatIdr())
                Text(
                    text = "Stok: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = onAdd,
                modifier = Modifier.semantics {
                    contentDescription = "Tambah ${product.name} ke keranjang"
                },
                enabled = product.stockQuantity > 0 && !isSubmitting
            ) { Text("Tambah") }
        }
    }
}

@Composable
private fun PaymentSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
