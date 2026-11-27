package id.berkahtigaputeri.basicpos.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.berkahtigaputeri.basicpos.data.backup.PosBackupService
import id.berkahtigaputeri.basicpos.data.preferences.UserPreferencesRepository
import id.berkahtigaputeri.basicpos.domain.model.StoreProfile
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: PosRepository,
    private val backupService: PosBackupService,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val profile: StateFlow<StoreProfile?> = repository
        .observeStoreProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val lowStockThreshold: StateFlow<Long> = preferencesRepository.lowStockThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferencesRepository.DEFAULT_LOW_STOCK_THRESHOLD)

    private val mutableMessage = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = mutableMessage.asStateFlow()

    private val mutableSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = mutableSaving.asStateFlow()

    private val mutableBackupBusy = MutableStateFlow(false)
    val isBackupBusy: StateFlow<Boolean> = mutableBackupBusy.asStateFlow()

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun saveProfile(profile: StoreProfile) {
        if (mutableSaving.value) return
        viewModelScope.launch {
            mutableSaving.value = true
            try {
                backupService.runExclusiveSettingsOperation {
                    repository.saveStoreProfile(profile)
                }
                mutableMessage.value = "Profil toko berhasil disimpan."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "Profil toko tidak dapat disimpan."
            } finally {
                mutableSaving.value = false
            }
        }
    }

    fun saveLowStockThreshold(value: Long) {
        viewModelScope.launch {
            try {
                backupService.setLowStockThreshold(value)
                mutableMessage.value = "Ambang stok rendah berhasil disimpan."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "Ambang stok tidak dapat disimpan."
            }
        }
    }

    fun exportBackup(uri: Uri) {
        if (mutableBackupBusy.value) return
        viewModelScope.launch {
            mutableBackupBusy.value = true
            try {
                backupService.exportTo(uri)
                mutableMessage.value = "Backup JSON berhasil dibuat. Simpan file di lokasi yang aman."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "Backup tidak dapat dibuat."
            } finally {
                mutableBackupBusy.value = false
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        if (mutableBackupBusy.value) return
        viewModelScope.launch {
            mutableBackupBusy.value = true
            try {
                val summary = backupService.restoreFrom(uri)
                mutableMessage.value =
                    "Pemulihan berhasil: ${summary.productCount} produk, ${summary.categoryCount} kategori, ${summary.transactionCount} transaksi. Ambang stok: ${summary.lowStockThreshold} unit."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableMessage.value = error.message ?: "File cadangan tidak valid atau gagal dipulihkan."
            } finally {
                mutableBackupBusy.value = false
            }
        }
    }

    class Factory(
        private val repository: PosRepository,
        private val backupService: PosBackupService,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository, backupService, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
