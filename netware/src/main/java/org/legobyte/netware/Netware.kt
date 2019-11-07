package org.legobyte.netware

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.CheckResult
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.io.Closeable

class Netware private constructor(private val context:Context) {

    // net event container. calls will observe this LiveData
    private val liveNetEvent =
        ActiveStateObserverLiveData<NetEvent> { isActive ->
            interceptor.shouldListen = isActive
        }

    // telephony-manager
    val telephonyManager = context.getSystemService<TelephonyManager>()!!
    // connectivity manager
    val connectivityManager = context.getSystemService<ConnectivityManager>()!!

    /**
     * publishes the event to observers
     */
    private val eventPublisher : NetEventListener = { event ->
        if(isMainThread)
            liveNetEvent.value = event
        else
            liveNetEvent.postValue(event)
    }

    /**
     *  android network observers
     */
    private val interceptor = when(Build.VERSION.SDK_INT){
        in 0..28 -> DefaultInterceptor(context.applicationContext, this, eventPublisher)
        else -> NewApiInterceptor(context.applicationContext, this, eventPublisher)
    }

    // Observe for NetEvent data in this Lifecycle scope
    fun observe(lifecycleOwner: LifecycleOwner, observer:Observer<NetEvent>): Registry {
        liveNetEvent.observe(lifecycleOwner, observer)
        return RegisteredObserver(this, observer)
    }

    /**
     *  observe for NetEvent data forever.
     *  Warning! you should call [Closeable.close] method when your done with observation
     */
    @CheckResult
    fun observeForever(observer: Observer<NetEvent>) : Registry {
        liveNetEvent.observeForever(observer)
        return RegisteredObserver(this, observer)
    }

    // Remove this observer from NetEvent data container
    fun removeObserver(observer: Observer<NetEvent>){
        liveNetEvent.removeObserver(observer)
    }

    // Removes all observers registered inside this Lifecycle
    fun removeObservers(lifecycleOwner: LifecycleOwner){
        liveNetEvent.removeObservers(lifecycleOwner)
    }


    // checks whether the device has active connection to network or not
    fun isConnectedToNetwork() : Boolean {
        try {
            var netInfo = connectivityManager.activeNetworkInfo
            if(netInfo?.isConnectedOrConnecting == true || netInfo?.isAvailable == true)
                return true
            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if(netInfo?.isConnectedOrConnecting == true)
                return true
            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if(netInfo?.isConnectedOrConnecting == true)
                return true
        }catch (ignored:Throwable){
            return true
        }
        return false//getSystemService<ConnectivityManager>()!!.activeNetworkInfo?.isConnectedOrConnecting == true
    }
    /**
     *  checks whether the connection is wifi connection
     */
    fun isConnectionWifi() = connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    /**
     *  checks whether the connection is data connection
     */
    fun icConnectionCellular() = connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
    /**
     *  checks whether the connection is roaming
     */
    fun isConnectionRoaming() = connectivityManager.activeNetworkInfo?.isRoaming == true

    /**
     *  checks whether the connection is slow or not
     *  only determined when the connection is cellular
     */
    fun isConnectionSlow() : Boolean {
        val type = telephonyManager.networkType
        return intArrayOf(
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_IDEN
        ).contains(type)
    }



    init {
        // set initial state
        // callbacks wont work if no network in initialization state
        if(!isConnectedToNetwork()){
            // set initial state
            liveNetEvent.postValue(NetEvent(OTHERS, DISCONNECTED, isConnectionSlow()))
        }
    }

    // gets netware instance
    companion object : Yekeje<Netware, Context>({
        // avoid memory-leak using applicationContext
        Netware(it.applicationContext)
    })
}

