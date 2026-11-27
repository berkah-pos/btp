package id.berkahtigaputeri.basicpos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.berkahtigaputeri.basicpos.data.preferences.UserPreferencesRepository
import id.berkahtigaputeri.basicpos.domain.model.DashboardSummary
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    repository: PosRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val summary: StateFlow<DashboardSummary?> = preferencesRepository.lowStockThreshold
        .flatMapLatest(repository::observeDashboardSummary)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    class Factory(
        private val repository: PosRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(repository, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
