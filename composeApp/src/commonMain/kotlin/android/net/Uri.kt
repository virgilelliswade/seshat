package android.net

class Uri(private val uriString: String) {
    override fun toString(): String = uriString
    val path: String? get() = if (uriString.startsWith("file://")) uriString.removePrefix("file://") else uriString

    companion object {
        fun parse(str: String): Uri = Uri(str)
    }
}
