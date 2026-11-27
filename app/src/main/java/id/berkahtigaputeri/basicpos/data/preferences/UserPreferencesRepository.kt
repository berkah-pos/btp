package id.berkahtigaputeri.basicpos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "berkah_pos_settings"
)

class UserPreferencesRepository(
    private val context: Context
) {
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color_enabled")

    val dynamicColorEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[dynamicColorKey] ?: true
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[dynamicColorKey] = enabled
        }
    }
}
