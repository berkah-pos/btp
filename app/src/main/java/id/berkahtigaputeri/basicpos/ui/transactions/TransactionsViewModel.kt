package id.berkahtigaputeri.basicpos.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.berkahtigaputeri.basicpos.domain.model.SaleReceipt
import id.berkahtigaputeri.basicpos.domain.model.SaleSummary
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
class TransactionsViewModel(
    private val repository: PosRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val transactions: StateFlow<List<SaleSummary>> = query
        .flatMapLatest(repository::observeTransactions)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val mutableReceipt = MutableStateFlow<SaleReceipt?>(null)
    val receipt: StateFlow<SaleReceipt?> = mutableReceipt.asStateFlow()

    private val mutableMessage = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = mutableMessage.asStateFlow()

    fun updateQuery(value: String) {
        query.value = value
    }

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun closeReceipt() {
        mutableReceipt.value = null
    }

    fun openReceipt(transactionId: Long) {
        viewModelScope.launch {
            try {
                mutableReceipt.value = repository.getTransactionReceipt(transactionId)
                if (mutableReceipt.value == null) {
                    mutableMessage.value = "Rincian transaksi tidak ditemukan."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "Rincian transaksi tidak dapat dibuka."
            }
        }
    }

    class Factory(
        private val repository: PosRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
                return TransactionsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
