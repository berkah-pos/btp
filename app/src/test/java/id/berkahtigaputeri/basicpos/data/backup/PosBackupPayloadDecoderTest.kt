package id.berkahtigaputeri.basicpos.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.charset.CharacterCodingException

class PosBackupPayloadDecoderTest {
    @Test
    fun decodesValidUtf8WithoutChangingIndonesianText() {
        val payload = """{"storeName":"Toko Berkah – Cabang Pagi"}"""

        assertEquals(payload, PosBackupPayloadDecoder.decodeUtf8(payload.toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun rejectsMalformedUtf8InsteadOfReplacingBytes() {
        val malformed = byteArrayOf(
            0x7B, 0x22, 0x78, 0x22, 0x3A, 0x22,
            0xC3.toByte(), 0x28,
            0x22, 0x7D
        )

        val failure = assertThrows(IllegalArgumentException::class.java) {
            PosBackupPayloadDecoder.decodeUtf8(malformed)
        }

        assertEquals("File cadangan bukan UTF-8 yang valid.", failure.message)
        assertEquals(true, failure.cause is CharacterCodingException)
    }

    @Test
    fun rejectsTruncatedMultibyteSequenceAtEndOfFile() {
        val truncated = byteArrayOf(0x7B, 0x22, 0x78, 0x22, 0x3A, 0x22, 0xE2.toByte())

        assertThrows(IllegalArgumentException::class.java) {
            PosBackupPayloadDecoder.decodeUtf8(truncated)
        }
    }
}
