@file:JvmName("NetwareNetworkInterceptors")
package org.legobyte.netware

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.core.content.getSystemService


abstract class Interceptor(protected val context: Context, protected val netware: Netware, private val listener: NetEventListener) : NetEventListener {
    abstract var shouldListen : Boolean

    override fun invoke(event: NetEvent) {
        listener(event)
    }
}


internal class DefaultInterceptor(context: Context, netware: Netware, listener: NetEventListener) : Interceptor(context, netware, listener) {
    private val mNetworkEventReceiver by lazy {
        NetworkChangeReceiver(this)
    }
    override var shouldListen = false
    set(value) {
        if (field == value)
            return
        field = value

        if (value) {
            context.registerReceiver(
                mNetworkEventReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        } else {
            context.unregisterReceiver(mNetworkEventReceiver)
        }
    }



    private inner class NetworkChangeReceiver(private val eventListener: NetEventListener) : BroadcastReceiver() {

        private var oldEvent:NetEvent? = null

        override fun onReceive(context: Context, intent: Intent) {
            // connectivity change event received
            if(ConnectivityManager.CONNECTIVITY_ACTION == intent.action){
                val networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo? ?: netware.connectivityManager.activeNetworkInfo ?: return
                val state= when(networkInfo.state){
                    NetworkInfo.State.CONNECTED -> CONNECTED
                    NetworkInfo.State.CONNECTING -> CONNECTING
                    NetworkInfo.State.DISCONNECTED -> DISCONNECTED
                    NetworkInfo.State.DISCONNECTING -> DISCONNECTING
                    NetworkInfo.State.SUSPENDED -> SUSPENDED
                    else -> UNKNOWN
                }
                val type = when(networkInfo.type){
                    ConnectivityManager.TYPE_MOBILE-> DATA
                    ConnectivityManager.TYPE_WIFI-> WIFI
                    ConnectivityManager.TYPE_VPN-> VPN
                    else -> OTHERS
                }
                val newEvent = NetEvent(type, state, netware.isConnectionSlow())
                if(newEvent == oldEvent)
                    return
                oldEvent = newEvent
                eventListener(newEvent)
            }
        }
    }

}

@TargetApi(24)
internal class NewApiInterceptor(context: Context, netware: Netware, listener: NetEventListener) :
    Interceptor(context, netware, listener) {
    private val callback by lazy {
        NetCallback(context, this)
    }
    override var shouldListen = false
        set(value) {
            if(field == value)
                return
            if(value){
                netware.connectivityManager.registerDefaultNetworkCallback(callback)
            }else{
                netware.connectivityManager.unregisterNetworkCallback(callback)
            }
            field = value
        }

    private inner class NetCallback(context: Context, private val eventListener: NetEventListener) : ConnectivityManager.NetworkCallback() {

        private val mConMan = context.getSystemService<ConnectivityManager>()!!

        private var oldEvent:NetEvent? = null

        override fun onAvailable(network: Network) {
            intercept(network, CONNECTED)
        }
        override fun onLost(network: Network) {
            intercept(network, DISCONNECTED)
        }

        override fun onUnavailable() {

        }
        override fun onLosing(network: Network, maxMsToLive: Int) {
            intercept(network, DISCONNECTING)
        }

        private fun intercept(network: Network, @State state:Int){
            val capabilities = mConMan.getNetworkCapabilities(network)
            val type = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true -> WIFI
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)==true -> DATA
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)==true -> VPN
                else -> OTHERS
            }
            val newEvent = NetEvent(type, state, netware.isConnectionSlow())
            if(newEvent == oldEvent)
                return
            oldEvent = newEvent
            eventListener(newEvent)
        }
    }

}


