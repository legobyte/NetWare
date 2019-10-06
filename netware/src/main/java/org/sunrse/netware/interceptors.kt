@file:JvmName("NetwareNetworkInterceptors")
package org.sunrse.netware

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import androidx.core.content.getSystemService

typealias NetEventListener = (event:NetEvent) -> Unit


abstract class Interceptor(protected val context: Context, private val listener: NetEventListener) : NetEventListener {
    abstract var shouldListen : Boolean

    override fun invoke(event: NetEvent) {
        listener(event)
    }
}

internal class DefaultInterceptor(context: Context, listener: NetEventListener) : Interceptor(context, listener) {


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

        private var oldState = NO_STATE

        override fun onReceive(context: Context, intent: Intent) {
            // connectivity change event received
            if(ConnectivityManager.CONNECTIVITY_ACTION == intent.action){
                val networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo? ?: return
                val state= networkInfo.mapState()
                if(state == oldState)
                    return
                oldState = state
                val type = networkInfo.mapType()

                eventListener(NetEvent(type, state))
            }
        }
    }


    @State
    internal fun NetworkInfo.mapState() = when(state){
        NetworkInfo.State.CONNECTED -> CONNECTED
        NetworkInfo.State.CONNECTING -> CONNECTING
        NetworkInfo.State.DISCONNECTED -> DISCONNECTED
        NetworkInfo.State.DISCONNECTING -> DISCONNECTING
        NetworkInfo.State.SUSPENDED -> SUSPENDED
        else -> UNKNOWN
    }

    @NetType
    internal fun NetworkInfo.mapType() = when(type){
        ConnectivityManager.TYPE_MOBILE-> DATA
        ConnectivityManager.TYPE_WIFI-> WIFI
        ConnectivityManager.TYPE_VPN-> VPN
        else -> OTHERS
    }




}

@TargetApi(24)
internal class NewApiInterceptor(context: Context, listener: NetEventListener) :
    Interceptor(context, listener) {
    private val mConMan = context.getSystemService<ConnectivityManager>()!!
    private val callback by lazy {
        NetCallback(context, this)
    }
    override var shouldListen = false
        set(value) {
            if(field == value)
                return
            if(value){
                mConMan.registerDefaultNetworkCallback(callback)
            }else{
                mConMan.unregisterNetworkCallback(callback)
            }
            field = value
        }

    private inner class NetCallback(context: Context, private val eventListener: NetEventListener) : ConnectivityManager.NetworkCallback() {

        private val mConMan = context.getSystemService<ConnectivityManager>()!!
        private var oldState : Int = NO_STATE

        override fun onAvailable(network: Network) {
            intercept(network, CONNECTED)
        }
        override fun onLost(network: Network) {
            intercept(network, DISCONNECTED)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            intercept(network, DISCONNECTING)
        }

        private fun intercept(network: Network, @State state:Int){
            if(state == oldState)
                return
            oldState = state
            val capabilities = mConMan.getNetworkCapabilities(network) ?: return
            eventListener(NetEvent(capabilities.mapType(), state))
        }
    }

    @NetType
    internal fun NetworkCapabilities.mapType() : Int {
        return when {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> WIFI
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> DATA
            hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> VPN
            else -> OTHERS
        }
    }
}


