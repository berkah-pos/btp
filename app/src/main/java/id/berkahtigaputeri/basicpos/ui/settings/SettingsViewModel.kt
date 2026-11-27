package id.berkahtigaputeri.basicpos.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val repository: PosRepository
) : ViewModel() {
    val profile: StateFlow<StoreProfile?> = repository
        .observeStoreProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val mutableMessage = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = mutableMessage.asStateFlow()

    private val mutableSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = mutableSaving.asStateFlow()

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun saveProfile(profile: StoreProfile) {
        if (mutableSaving.value) return
        viewModelScope.launch {
            mutableSaving.value = true
            try {
                repository.saveStoreProfile(profile)
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

    class Factory(
        private val repository: PosRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
