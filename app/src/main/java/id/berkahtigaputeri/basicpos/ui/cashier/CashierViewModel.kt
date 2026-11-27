package id.berkahtigaputeri.basicpos.ui.cashier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import id.berkahtigaputeri.basicpos.domain.model.CheckoutLine
import id.berkahtigaputeri.basicpos.domain.model.CheckoutRequest
import id.berkahtigaputeri.basicpos.domain.model.Product
import id.berkahtigaputeri.basicpos.domain.model.SaleReceipt
import id.berkahtigaputeri.basicpos.domain.model.StoreProfile
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CashierCartItem(
    val product: Product,
    val quantity: Long
)

class CashierViewModel(
    private val repository: PosRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val inventory = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val storeProfile: StateFlow<StoreProfile?> = repository
        .observeStoreProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val products: StateFlow<List<Product>> = query
        .flatMapLatest(repository::observeProducts)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableCart = MutableStateFlow<List<CashierCartItem>>(emptyList())
    val cart: StateFlow<List<CashierCartItem>> = mutableCart.asStateFlow()

    private val mutableMessage = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = mutableMessage.asStateFlow()

    private val mutableReceipt = MutableStateFlow<SaleReceipt?>(null)
    val receipt: StateFlow<SaleReceipt?> = mutableReceipt.asStateFlow()

    private val mutableSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = mutableSubmitting.asStateFlow()

    init {
        viewModelScope.launch {
            inventory.collect { latestProducts ->
                val latestById = latestProducts.associateBy { it.id }
                mutableCart.value = mutableCart.value.map { item ->
                    val latest = latestById[item.product.id]
                    item.copy(
                        product = latest ?: item.product.copy(stockQuantity = 0)
                    )
                }
            }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun addProduct(product: Product) {
        val current = mutableCart.value
        val existing = current.firstOrNull { it.product.id == product.id }
        val nextQuantity = runCatching {
            Math.addExact(existing?.quantity ?: 0L, 1L)
        }.getOrElse {
            mutableMessage.value = "Jumlah produk terlalu besar."
            return
        }
        if (product.stockQuantity < nextQuantity) {
            mutableMessage.value = "Stok ${product.name} tidak mencukupi."
            return
        }
        mutableCart.value = if (existing == null) {
            current + CashierCartItem(product, 1L)
        } else {
            current.map {
                if (it.product.id == product.id) it.copy(quantity = nextQuantity) else it
            }
        }
        mutableMessage.value = null
    }

    fun changeQuantity(productId: Long, delta: Long) {
        mutableCart.value = mutableCart.value.mapNotNull { item ->
            if (item.product.id != productId) return@mapNotNull item
            val next = runCatching { Math.addExact(item.quantity, delta) }.getOrNull()
                ?: run {
                    mutableMessage.value = "Jumlah produk terlalu besar."
                    return@mapNotNull item
                }
            when {
                next <= 0 -> null
                next > item.product.stockQuantity -> {
                    mutableMessage.value = "Jumlah melebihi stok ${item.product.name}."
                    item
                }
                else -> item.copy(quantity = next)
            }
        }
    }

    fun removeProduct(productId: Long) {
        mutableCart.value = mutableCart.value.filterNot { it.product.id == productId }
    }

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun clearReceipt() {
        mutableReceipt.value = null
    }

    fun checkout(
        discountRupiah: Long,
        taxRupiah: Long,
        paidRupiah: Long
    ) {
        if (mutableSubmitting.value) return
        val currentCart = mutableCart.value
        if (currentCart.isEmpty()) {
            mutableMessage.value = "Keranjang masih kosong."
            return
        }

        viewModelScope.launch {
            mutableSubmitting.value = true
            try {
                val savedReceipt = repository.checkout(
                    CheckoutRequest(
                        lines = currentCart.map {
                            CheckoutLine(productId = it.product.id, quantity = it.quantity)
                        },
                        discountRupiah = discountRupiah,
                        taxRupiah = taxRupiah,
                        paidRupiah = paidRupiah
                    )
                )
                mutableCart.value = emptyList()
                mutableReceipt.value = savedReceipt
                mutableMessage.value = "Transaksi berhasil disimpan."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "Transaksi tidak dapat disimpan."
            } finally {
                mutableSubmitting.value = false
            }
        }
    }

    class Factory(
        private val repository: PosRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CashierViewModel::class.java)) {
                return CashierViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
