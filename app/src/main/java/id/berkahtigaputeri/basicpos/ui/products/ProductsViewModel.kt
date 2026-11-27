package id.berkahtigaputeri.basicpos.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.berkahtigaputeri.basicpos.domain.model.Category
import id.berkahtigaputeri.basicpos.domain.model.Product
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(
    private val repository: PosRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val products: StateFlow<List<Product>> = query
        .flatMapLatest(repository::observeProducts)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = repository
        .observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableMessage = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = mutableMessage.asStateFlow()

    fun updateQuery(value: String) {
        query.value = value
    }

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun saveProduct(product: Product) = execute(
        success = "Produk berhasil disimpan.",
        fallback = "Produk tidak dapat disimpan."
    ) {
        repository.saveProduct(product)
    }

    fun deleteProduct(productId: Long) = execute(
        success = "Produk berhasil dihapus.",
        fallback = "Produk tidak dapat dihapus."
    ) {
        repository.deleteProduct(productId)
    }

    fun saveCategory(name: String) = execute(
        success = "Kategori berhasil ditambahkan.",
        fallback = "Kategori tidak dapat disimpan."
    ) {
        repository.saveCategory(Category(name = name))
    }

    fun seedSampleProducts() = execute(
        success = "Tiga produk contoh ditambahkan. Tidak ada transaksi contoh yang dibuat.",
        fallback = "Data contoh tidak dapat ditambahkan."
    ) {
        repository.seedSampleProducts()
    }

    private fun execute(
        success: String,
        fallback: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                action()
                mutableMessage.value = success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: fallback
            }
        }
    }

    class Factory(
        private val repository: PosRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                return ProductsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
