package org.sunrse.netware

import android.content.Context
import android.os.Build
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.io.Closeable

class Netware private constructor(context:Context) {

    // net event container. calls will observe this LiveData
    private val liveNetEvent = ActiveStateObserverLiveData<NetEvent>{ isActive->
        interceptor.shouldListen = isActive
    }

    // listens for NetEvents and publishes this event to observers
    private val netEventListener : NetEventListener = {event ->
        if(isMainThread)
            liveNetEvent.value = event
        else
            liveNetEvent.postValue(event)
    }

    // listener for NetEvents
    private val interceptor = when(Build.VERSION.SDK_INT){
        in 0..24 -> DefaultInterceptor(context.applicationContext, netEventListener)
        else -> NewApiInterceptor(context.applicationContext, netEventListener)
    }

    // Observe for NetEvent data in this Lifecycle scope
    fun observe(lifecycleOwner: LifecycleOwner, observer:Observer<NetEvent>):Registry{
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

    // gets netware instance
    companion object : Yekeje<Netware, Context>({
        // avoid memory-leak using applicationContext
        Netware(it.applicationContext)
    })
}

