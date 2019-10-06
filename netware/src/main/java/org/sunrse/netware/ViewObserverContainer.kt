package org.sunrse.netware

import android.view.View
import androidx.lifecycle.Observer
import java.lang.ref.WeakReference

/**
 *  this class observers the data while the view is in attached state
 */
class ViewObserverContainer(private val netware: Netware, view:View, observer: Observer<NetEvent>) {

    // weak reference to the observer object
    private val weakObserver = WeakReference(observer)

    // view state listener
    private val attachStateChangeListener = object : View.OnAttachStateChangeListener{

        // the registered observer
        private var registry : Registry? = null

        override fun onViewDetachedFromWindow(v: View?) {
            // unregister the observer if any
            registry?.unregister()
            registry = null
        }
        override fun onViewAttachedToWindow(v: View?) {
            weakObserver.get()?.let {
                // the observer is not yet garbage-collected
                registry = netware.observeForever(observer)
            }
        }
    }
    init {
        view.addOnAttachStateChangeListener(attachStateChangeListener)
    }

}