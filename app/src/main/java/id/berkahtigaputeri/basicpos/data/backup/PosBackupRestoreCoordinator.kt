package id.berkahtigaputeri.basicpos.data.backup

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Coordinates the DataStore preference update with the Room restore transaction.
 *
 * The stores do not share an ACID transaction. If either phase throws (including cancellation),
 * restore the prior preference in a non-cancellable context. If compensation also fails, retain
 * that failure as suppressed information on the original exception.
 */
internal suspend fun <T> restoreWithLowStockPreferenceCompensation(
    previousLowStockThreshold: Long,
    restoredLowStockThreshold: Long,
    updateLowStockThreshold: suspend (Long) -> Unit,
    restoreRoom: suspend () -> T
): T {
    try {
        updateLowStockThreshold(restoredLowStockThreshold)
        return restoreRoom()
    } catch (failure: Throwable) {
        withContext(NonCancellable) {
            val compensationFailure = runCatching {
                updateLowStockThreshold(previousLowStockThreshold)
            }.exceptionOrNull()
            if (compensationFailure != null && compensationFailure !== failure) {
                failure.addSuppressed(compensationFailure)
            }
        }
        throw failure
    }
}
