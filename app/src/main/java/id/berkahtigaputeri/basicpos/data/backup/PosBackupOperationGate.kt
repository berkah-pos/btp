package id.berkahtigaputeri.basicpos.data.backup

import kotlinx.coroutines.sync.Mutex

/**
 * Serializes backup snapshots, restore replacement, and settings mutations that affect them.
 *
 * The gate is owned by the single PosBackupService held in BerkahPosApplication.AppContainer.
 * Explicit lock/unlock keeps the protected operation suspendable while guaranteeing release on
 * failure or cancellation.
 */
internal class PosBackupOperationGate {
    private val mutex = Mutex()

    suspend fun <T> withExclusiveOperation(block: suspend () -> T): T {
        mutex.lock()
        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }
}
