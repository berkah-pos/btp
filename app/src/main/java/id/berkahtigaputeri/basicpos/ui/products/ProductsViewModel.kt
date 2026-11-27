package id.berkahtigaputeri.basicpos.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.berkahtigaputeri.basicpos.domain.model.Category
import id.berkahtigaputeri.basicpos.domain.model.Product
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            runCatching { repository.saveProduct(product) }
                .onSuccess { mutableMessage.value = "Produk berhasil disimpan." }
                .onFailure { mutableMessage.value = it.message ?: "Produk tidak dapat disimpan." }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(productId) }
                .onSuccess { mutableMessage.value = "Produk berhasil dihapus." }
                .onFailure { mutableMessage.value = it.message ?: "Produk tidak dapat dihapus." }
        }
    }

    fun saveCategory(name: String) {
        viewModelScope.launch {
            runCatching { repository.saveCategory(Category(name = name)) }
                .onSuccess { mutableMessage.value = "Kategori berhasil ditambahkan." }
                .onFailure { mutableMessage.value = it.message ?: "Kategori tidak dapat disimpan." }
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
