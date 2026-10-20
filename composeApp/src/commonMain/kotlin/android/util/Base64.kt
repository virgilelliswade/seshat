package android.util

object Base64 {
    const val DEFAULT = 0
    const val NO_WRAP = 2

    fun decode(str: String, flags: Int = DEFAULT): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(str.trim())
        } catch (_: Exception) {
            java.util.Base64.getMimeDecoder().decode(str.trim())
        }
    }

    fun encodeToString(bytes: ByteArray, flags: Int = DEFAULT): String {
        return java.util.Base64.getEncoder().encodeToString(bytes)
    }
}
