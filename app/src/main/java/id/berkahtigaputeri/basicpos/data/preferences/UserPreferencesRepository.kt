package id.berkahtigaputeri.basicpos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "berkah_pos_settings"
)

class UserPreferencesRepository(
    private val context: Context
) {
    private val lowStockThresholdKey = longPreferencesKey("low_stock_threshold")

    val lowStockThreshold: Flow<Long> = context.settingsDataStore.data.map { preferences ->
        preferences[lowStockThresholdKey] ?: DEFAULT_LOW_STOCK_THRESHOLD
    }

    suspend fun setLowStockThreshold(value: Long) {
        require(value in MIN_LOW_STOCK_THRESHOLD..MAX_LOW_STOCK_THRESHOLD) {
            "Ambang stok harus berada di antara $MIN_LOW_STOCK_THRESHOLD dan $MAX_LOW_STOCK_THRESHOLD."
        }
        context.settingsDataStore.edit { preferences ->
            preferences[lowStockThresholdKey] = value
        }
    }

    companion object {
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5L
        const val MIN_LOW_STOCK_THRESHOLD = 0L
        const val MAX_LOW_STOCK_THRESHOLD = 1_000_000L
    }
}
