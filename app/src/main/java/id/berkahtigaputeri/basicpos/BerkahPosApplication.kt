package id.berkahtigaputeri.basicpos

import android.app.Application
import androidx.room.Room
import id.berkahtigaputeri.basicpos.data.backup.PosBackupService
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.preferences.UserPreferencesRepository
import id.berkahtigaputeri.basicpos.data.repository.PosRepositoryImpl
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository

class BerkahPosApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            BerkahPosDatabase::class.java,
            "berkah-pos.db"
        ).build()
        container = AppContainer(
            posRepository = PosRepositoryImpl(database),
            backupService = PosBackupService(database, contentResolver),
            preferencesRepository = UserPreferencesRepository(applicationContext)
        )
    }
}

class AppContainer(
    val posRepository: PosRepository,
    val backupService: PosBackupService,
    val preferencesRepository: UserPreferencesRepository
)
