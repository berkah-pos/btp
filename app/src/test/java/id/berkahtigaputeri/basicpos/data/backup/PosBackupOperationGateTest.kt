package id.berkahtigaputeri.basicpos.data.backup

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PosBackupOperationGateTest {
    @Test
    fun doesNotOverlapSettingsMutationWithBackupOperation() = runBlocking {
        val gate = PosBackupOperationGate()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()

        val backup = launch {
            gate.withExclusiveOperation {
                firstEntered.complete(Unit)
                releaseFirst.await()
            }
        }
        firstEntered.await()

        val settingsMutation = launch {
            gate.withExclusiveOperation {
                secondEntered.complete(Unit)
            }
        }
        yield()

        assertFalse(secondEntered.isCompleted)
        releaseFirst.complete(Unit)
        backup.join()
        settingsMutation.join()
        assertTrue(secondEntered.isCompleted)
    }

    @Test
    fun releasesGateWhenProtectedOperationIsCancelled() = runBlocking {
        val gate = PosBackupOperationGate()
        val entered = CompletableDeferred<Unit>()

        val cancelledOperation = launch {
            gate.withExclusiveOperation {
                entered.complete(Unit)
                awaitCancellation()
            }
        }
        entered.await()
        cancelledOperation.cancelAndJoin()

        assertEquals("next operation", gate.withExclusiveOperation { "next operation" })
    }
}
