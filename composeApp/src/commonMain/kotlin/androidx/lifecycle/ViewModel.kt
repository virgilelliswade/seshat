package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.Collections
import java.util.WeakHashMap

private val scopes = Collections.synchronizedMap(WeakHashMap<ViewModel, CoroutineScope>())

open class ViewModel {
    open fun onCleared() {
        scopes.remove(this)?.cancel()
    }
}

val ViewModel.viewModelScope: CoroutineScope
    get() = scopes.getOrPut(this) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
