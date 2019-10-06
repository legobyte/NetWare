package org.sunrse.netware

import androidx.lifecycle.MutableLiveData


// !! visibility modifier has been removed so you can use this classes if you want

/**
 *  callback for state of @ActiveStateObserverLiveData
 */
/*internal */
typealias ActiveStateChangeCallback = (isActive:Boolean) -> Unit


// A super-simple LiveData subclass that notifies about active status of LifeCycles
/*internal */
class ActiveStateObserverLiveData<T>(private val callback:ActiveStateChangeCallback) : MutableLiveData<T>() {


    /**
     * No active observers present for the moment
     * we may have observers but not active for the moment like when user presses home button
     * [androidx.lifecycle.Lifecycle] will trigger the [androidx.lifecycle.Lifecycle.Event.ON_PAUSE] event,
     * if the user comes back to application, lifecycle will trigger the [androidx.lifecycle.Lifecycle.Event.ON_RESUME] event
     *  and [onActive] function will be called if no active observers existed
     */
    override fun onInactive() {
        super.onInactive()
        callback(false)
    }

    /**
     * We have at least one active lifecycle observing for data
     * at least one [androidx.lifecycle.Lifecycle] with state of [androidx.lifecycle.Lifecycle.State.STARTED] or above
     */
    override fun onActive() {
        super.onActive()
        callback(true)
    }

}
