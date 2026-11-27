package id.berkahtigaputeri.basicpos.data.backup

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Coordinates the DataStore preference update with the Room restore transaction.
 *
 * Room and DataStore do not share an ACID transaction. Check cancellation before starting, then
 * keep the short cross-store mutation sequence non-cancellable so caller cancellation cannot
 * arrive after Room commits but before this function returns and incorrectly roll back the
 * preference. If either operation itself throws, compensate the preference in the same protected
 * section and retain a compensation failure as suppressed information.
 *
 * This protects against coroutine cancellation, not process death or power loss between stores.
 */
internal suspend fun <T> restoreWithLowStockPreferenceCompensation(
    previousLowStockThreshold: Long,
    restoredLowStockThreshold: Long,
    updateLowStockThreshold: suspend (Long) -> Unit,
    restoreRoom: suspend () -> T
): T {
    currentCoroutineContext().ensureActive()

    return withContext(NonCancellable) {
        try {
            updateLowStockThreshold(restoredLowStockThreshold)
            restoreRoom()
        } catch (failure: Throwable) {
            val compensationFailure = runCatching {
                updateLowStockThreshold(previousLowStockThreshold)
            }.exceptionOrNull()
            if (compensationFailure != null && compensationFailure !== failure) {
                failure.addSuppressed(compensationFailure)
            }
            throw failure
        }
    }
}
