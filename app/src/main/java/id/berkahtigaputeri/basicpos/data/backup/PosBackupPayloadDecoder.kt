package id.berkahtigaputeri.basicpos.data.backup

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Decodes backup bytes without silently replacing malformed UTF-8 sequences.
 *
 * A replacement character could otherwise alter a shop name, SKU, receipt snapshot, or other
 * user data while still allowing the surrounding JSON document to parse.
 */
internal object PosBackupPayloadDecoder {
    fun decodeUtf8(bytes: ByteArray): String {
        return try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (failure: CharacterCodingException) {
            throw IllegalArgumentException("File cadangan bukan UTF-8 yang valid.", failure)
        }
    }
}
