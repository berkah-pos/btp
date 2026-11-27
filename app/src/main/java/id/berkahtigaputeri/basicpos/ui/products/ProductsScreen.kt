package id.berkahtigaputeri.basicpos.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import id.berkahtigaputeri.basicpos.core.money.Rupiah
import id.berkahtigaputeri.basicpos.core.money.formatIdr
import id.berkahtigaputeri.basicpos.domain.model.Category
import id.berkahtigaputeri.basicpos.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val message by viewModel.message.collectAsState()
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var deletingProduct by remember { mutableStateOf<Product?>(null) }
    var showSampleConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produk") },
                actions = {
                    TextButton(onClick = { showCategoryDialog = true }) {
                        Text("Kategori")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    showProductDialog = true
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari nama produk atau SKU") }
            )

            if (message != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(message!!, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::dismissMessage) {
                            Text("Tutup")
                        }
                    }
                }
            }

            if (products.isEmpty()) {
                if (query.isBlank()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Belum ada produk. Tekan + untuk menambahkan produk pertama.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = { showSampleConfirmation = true }) {
                            Text("Coba dengan data contoh")
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 32.dp)) {
                        Text(
                            "Produk tidak ditemukan.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            category = categories.firstOrNull { it.id == product.categoryId },
                            onEdit = {
                                editingProduct = product
                                showProductDialog = true
                            },
                            onDelete = { deletingProduct = product }
                        )
                    }
                }
            }
        }
    }

    if (showProductDialog) {
        ProductEditorDialog(
            product = editingProduct,
            categories = categories,
            onDismiss = { showProductDialog = false },
            onSave = {
                viewModel.saveProduct(it)
                showProductDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        CategoryEditorDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = {
                viewModel.saveCategory(it)
                showCategoryDialog = false
            }
        )
    }

    if (showSampleConfirmation) {
        AlertDialog(
            onDismissRequest = { showSampleConfirmation = false },
            title = { Text("Tambahkan data contoh?") },
            text = {
                Text(
                    "Tiga produk dengan awalan [CONTOH] akan ditambahkan ke katalog. Tidak ada transaksi atau penjualan contoh yang dibuat. Produk contoh dapat diedit atau dihapus."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSampleConfirmation = false
                    viewModel.seedSampleProducts()
                }) { Text("Tambahkan contoh") }
            },
            dismissButton = {
                TextButton(onClick = { showSampleConfirmation = false }) { Text("Batal") }
            }
        )
    }

    deletingProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("Hapus produk?") },
            text = { Text("Produk ${product.name} akan dihapus dari daftar produk. Riwayat struk lama tetap disimpan.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProduct(product.id)
                    deletingProduct = null
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = Rupiah.from(product.priceRupiah).formatIdr(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Stok: ${product.stockQuantity} unit  •  SKU: ${product.sku ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (category != null) {
                Text(
                    text = "Kategori: ${category.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Hapus") }
            }
        }
    }
}

@Composable
private fun ProductEditorDialog(
    product: Product?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember(product?.id) { mutableStateOf(product?.name.orEmpty()) }
    var sku by remember(product?.id) { mutableStateOf(product?.sku.orEmpty()) }
    var price by remember(product?.id) {
        mutableStateOf(product?.priceRupiah?.toString().orEmpty())
    }
    var stock by remember(product?.id) {
        mutableStateOf(product?.stockQuantity?.toString() ?: "0")
    }
    var categoryId by remember(product?.id) { mutableStateOf(product?.categoryId) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Tambah produk" else "Edit produk") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nama produk") }
                )
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("SKU (opsional)") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Harga jual (Rupiah)") }
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Stok") }
                )
                Box {
                    OutlinedButton(
                        onClick = { categoryMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            categories.firstOrNull { it.id == categoryId }?.name
                                ?: "Tanpa kategori"
                        )
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tanpa kategori") },
                            onClick = {
                                categoryId = null
                                categoryMenuExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    categoryId = category.id
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                if (validationMessage != null) {
                    Text(
                        text = validationMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedPrice = price.toLongOrNull()
                val parsedStock = stock.toLongOrNull()
                validationMessage = when {
                    name.isBlank() -> "Nama produk wajib diisi."
                    parsedPrice == null -> "Harga tidak valid."
                    parsedStock == null -> "Stok tidak valid."
                    else -> null
                }
                if (validationMessage == null) {
                    onSave(
                        Product(
                            id = product?.id ?: 0,
                            name = name,
                            sku = sku,
                            priceRupiah = parsedPrice!!,
                            stockQuantity = parsedStock!!,
                            categoryId = categoryId
                        )
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun CategoryEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah kategori") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nama kategori") }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name) },
                enabled = name.isNotBlank()
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
