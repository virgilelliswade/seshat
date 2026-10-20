package androidx.activity.result.contract

import android.net.Uri

abstract class ActivityResultContract<I, O>

object ActivityResultContracts {
    class GetContent : ActivityResultContract<String, Uri?>()
}
