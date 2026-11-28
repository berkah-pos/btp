package id.berkahtigaputeri.basicpos

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import java.io.File

class BerkahPosTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(
        cl,
        BerkahPosTestApplication::class.java.name,
        context
    )
}

/**
 * Instrumentation uses separate Room and DataStore files so UI tests never read or mutate
 * a real installation's POS records or preferences.
 */
class BerkahPosTestApplication : BerkahPosApplication() {
    override val databaseName: String = "berkah-pos-instrumentation.db"
    override val preferencesName: String = "berkah_pos_settings_instrumentation"

    val instrumentationDatabase: BerkahPosDatabase
        get() = database

    override fun onCreate() {
        deleteDatabase(databaseName)
        File(File(filesDir, "datastore"), "$preferencesName.preferences_pb").delete()
        super.onCreate()
    }
}
