package androidx.activity.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.activity.result.contract.ActivityResultContract

interface ManagedActivityResultLauncher<I, O> {
    fun launch(input: I)
}

@Suppress("UNCHECKED_CAST")
@Composable
fun <I, O> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): ManagedActivityResultLauncher<I, O> {
    return remember {
        object : ManagedActivityResultLauncher<I, O> {
            override fun launch(input: I) {
                try {
                    val fd = java.awt.FileDialog(null as java.awt.Frame?, "Select Image", java.awt.FileDialog.LOAD)
                    fd.setFilenameFilter { _, name ->
                        val n = name.lowercase()
                        n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".webp")
                    }
                    fd.isVisible = true
                    val file = fd.file
                    val dir = fd.directory
                    if (file != null && dir != null) {
                        val path = java.io.File(dir, file).absolutePath
                        val uri = Uri.parse("file://$path")
                        onResult(uri as O)
                    }
                } catch (_: Throwable) {}
            }
        }
    }
}
