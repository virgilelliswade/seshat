package android.widget

import com.seshat.writer.platform.AppPlatform

object Toast {
    const val LENGTH_SHORT = 0
    const val LENGTH_LONG = 1

    fun makeText(context: Any?, message: CharSequence?, duration: Int): ToastInstance {
        return ToastInstance(message?.toString() ?: "")
    }

    class ToastInstance(private val msg: String) {
        fun show() {
            AppPlatform.showToast(msg)
        }
    }
}
