package id.berkahtigaputeri.basicpos.data.backup

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PosBackupRestoreCoordinatorTest {
    @Test
    fun updatesPreferenceBeforeRoomAndReturnsRestoreResult() = runBlocking {
        val calls = mutableListOf<String>()

        val result = restoreWithLowStockPreferenceCompensation(
            previousLowStockThreshold = 5,
            restoredLowStockThreshold = 12,
            updateLowStockThreshold = { calls += "preference:$it" }
        ) {
            calls += "room"
            "restored"
        }

        assertEquals("restored", result)
        assertEquals(listOf("preference:12", "room"), calls)
    }

    @Test
    fun restoresPreviousPreferenceWhenRoomRestoreFails() = runBlocking {
        val calls = mutableListOf<Long>()
        val failure = IllegalStateException("Room transaction failed")

        val thrown = try {
            restoreWithLowStockPreferenceCompensation(
                previousLowStockThreshold = 5,
                restoredLowStockThreshold = 12,
                updateLowStockThreshold = { calls += it }
            ) {
                throw failure
            }
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertSame(failure, thrown)
        assertEquals(listOf(12L, 5L), calls)
    }

    @Test
    fun restoresPreviousPreferenceWhenNewPreferenceWriteFails() = runBlocking {
        val calls = mutableListOf<Long>()
        val failure = IllegalStateException("DataStore write failed")
        var firstWrite = true
        var roomInvoked = false

        val thrown = try {
            restoreWithLowStockPreferenceCompensation(
                previousLowStockThreshold = 5,
                restoredLowStockThreshold = 12,
                updateLowStockThreshold = {
                    calls += it
                    if (firstWrite) {
                        firstWrite = false
                        throw failure
                    }
                }
            ) {
                roomInvoked = true
            }
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertSame(failure, thrown)
        assertFalse(roomInvoked)
        assertEquals(listOf(12L, 5L), calls)
    }

    @Test
    fun compensatesPreferenceWhenRoomRestoreThrowsCancellation() = runBlocking {
        val calls = mutableListOf<Long>()
        val cancellation = CancellationException("Restore cancelled")

        val thrown = try {
            restoreWithLowStockPreferenceCompensation(
                previousLowStockThreshold = 5,
                restoredLowStockThreshold = 12,
                updateLowStockThreshold = { calls += it }
            ) {
                throw cancellation
            }
            null
        } catch (error: CancellationException) {
            error
        }

        assertSame(cancellation, thrown)
        assertEquals(listOf(12L, 5L), calls)
    }

    @Test
    fun callerCancellationCannotSplitPreferenceAndRoomRestore() = runBlocking {
        val roomStarted = CompletableDeferred<Unit>()
        val allowRoomCommit = CompletableDeferred<Unit>()
        val preferenceWrites = mutableListOf<Long>()
        var roomCommitted = false

        val restoreJob = launch {
            restoreWithLowStockPreferenceCompensation(
                previousLowStockThreshold = 5,
                restoredLowStockThreshold = 12,
                updateLowStockThreshold = { preferenceWrites += it }
            ) {
                roomStarted.complete(Unit)
                allowRoomCommit.await()
                roomCommitted = true
            }
        }

        roomStarted.await()
        restoreJob.cancel()
        allowRoomCommit.complete(Unit)
        restoreJob.join()

        assertTrue(roomCommitted)
        assertEquals(listOf(12L), preferenceWrites)
    }

    @Test
    fun preservesCompensationFailureAsSuppressedException() = runBlocking {
        val originalFailure = IllegalStateException("Room transaction failed")
        val compensationFailure = IllegalStateException("Preference rollback failed")

        val thrown = try {
            restoreWithLowStockPreferenceCompensation(
                previousLowStockThreshold = 5,
                restoredLowStockThreshold = 12,
                updateLowStockThreshold = {
                    if (it == 12L) throw originalFailure
                    throw compensationFailure
                }
            ) {
                Unit
            }
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertSame(originalFailure, thrown)
        assertTrue(thrown!!.suppressed.contains(compensationFailure))
    }
}
