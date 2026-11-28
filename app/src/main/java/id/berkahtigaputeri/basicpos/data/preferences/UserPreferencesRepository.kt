package id.berkahtigaputeri.basicpos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    context: Context,
    dataStoreName: String = DEFAULT_DATASTORE_NAME
) {
    private val settingsDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(dataStoreName) }
        )

    private val lowStockThresholdKey = longPreferencesKey("low_stock_threshold")

    val lowStockThreshold: Flow<Long> = settingsDataStore.data.map { preferences ->
        preferences[lowStockThresholdKey] ?: DEFAULT_LOW_STOCK_THRESHOLD
    }

    suspend fun setLowStockThreshold(value: Long) {
        require(value in MIN_LOW_STOCK_THRESHOLD..MAX_LOW_STOCK_THRESHOLD) {
            "Ambang stok harus berada di antara $MIN_LOW_STOCK_THRESHOLD dan $MAX_LOW_STOCK_THRESHOLD."
        }
        settingsDataStore.edit { preferences ->
            preferences[lowStockThresholdKey] = value
        }
    }

    companion object {
        const val DEFAULT_DATASTORE_NAME = "berkah_pos_settings"
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5L
        const val MIN_LOW_STOCK_THRESHOLD = 0L
        const val MAX_LOW_STOCK_THRESHOLD = 1_000_000L
    }
}
