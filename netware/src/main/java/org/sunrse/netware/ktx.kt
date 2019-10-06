@file:JvmName("NetwareExtensions")
package org.sunrse.netware

import android.content.Context
import android.net.ConnectivityManager
import android.os.Looper
import android.view.View
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import java.lang.IllegalStateException

/**
 *  gets the Netware instance with [androidx.fragment.app.Fragment]
 *  @return Netware
 */
val Fragment.netware: Netware
    get() = Netware.getInstance(context ?: throw IllegalStateException("Fragment is not attached to context"))
// gets the Netware instance with context
val Context.netware: Netware
    get() = Netware.getInstance(this)

// fluent api
infix fun Netware.with(lifecycleOwner: LifecycleOwner) : Fluent {
    return Fluent(this, lifecycleOwner)
}
infix fun Netware.with(view: View) : FluentView {
    return FluentView(this, view)
}


infix fun Fluent.observe(listener:NetEventListener) {
    netware.observe(owner, Observer{listener(it)})
}
infix fun FluentView.observe(listener:NetEventListener) {
    ViewObserverContainer(netware, view, Observer{listener(it)})
}

data class Fluent(val netware: Netware, val owner:LifecycleOwner)
data class FluentView(val netware: Netware, val view: View)

// checks whether the current thread is MainThread or not
val isMainThread : Boolean
    get() = Looper.getMainLooper().thread === Thread.currentThread()

val Context.isConnectedToNetwork : Boolean
    get() = getSystemService<ConnectivityManager>()!!.activeNetworkInfo?.isConnectedOrConnecting == true
val Context.isWifiConnection : Boolean
    get() = getSystemService<ConnectivityManager>()!!.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
val Context.isCellularConnection : Boolean
    get() = getSystemService<ConnectivityManager>()!!.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE

