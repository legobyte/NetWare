package org.sunrse.netware

import androidx.lifecycle.Observer

class RegisteredObserver(private val netware: Netware, private val observer:Observer<NetEvent>) : Registry {

    override fun unregister() {
        netware.removeObserver(observer)
    }
}