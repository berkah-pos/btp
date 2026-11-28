package id.berkahtigaputeri.basicpos

import android.app.Application
import androidx.room.Room
import id.berkahtigaputeri.basicpos.data.backup.PosBackupService
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.preferences.UserPreferencesRepository
import id.berkahtigaputeri.basicpos.data.repository.PosRepositoryImpl
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository

open class BerkahPosApplication : Application() {
    protected open val databaseName: String = "berkah-pos.db"
    protected open val preferencesName: String = UserPreferencesRepository.DEFAULT_DATASTORE_NAME
    protected lateinit var database: BerkahPosDatabase

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            BerkahPosDatabase::class.java,
            databaseName
        )
            .addMigrations(BerkahPosDatabase.MIGRATION_1_2)
            .build()
        val preferencesRepository = UserPreferencesRepository(applicationContext, preferencesName)
        container = AppContainer(
            posRepository = PosRepositoryImpl(database),
            preferencesRepository = preferencesRepository,
            backupService = PosBackupService(
                database = database,
                contentResolver = contentResolver,
                preferencesRepository = preferencesRepository
            )
        )
    }
}

class AppContainer(
    val posRepository: PosRepository,
    val backupService: PosBackupService,
    val preferencesRepository: UserPreferencesRepository
)
