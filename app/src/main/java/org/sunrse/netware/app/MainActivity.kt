package org.sunrse.netware.app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.legobyte.netware.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    private lateinit var registry: Registry

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // this observer will be unregistered automatically based on the lifecycle state
        Netware.getInstance(context=this).observe(lifecycleOwner = this, observer = Observer { event ->
            //
        })


        // you should call registry.unregister() method by yourself. or memory leak will occur.
        registry = Netware.getInstance(context = this).observeForever(Observer {

        })

        // `this` is an instance of LifeCycleOwner. `Activity` or `Fragment` or any component that is an instance of `LifeCycleOwner`
        netware with this observe {event->

            val connectionStatusText = when(event.state){
                DISCONNECTED, DISCONNECTING, SUSPENDED -> "No Network"
                CONNECTING, CONNECTED -> "Connected" // equals to event.hasActiveConnection
                else -> "Unknown"
            }
            val connectionTypeText = when(event.type){
                DATA -> "Mobile data"
                WIFI -> "Wifi"
                VPN -> "VPN"
                // maybe no network. check with event.state
                else -> ""
            }

            Log.d("NetWareAct", "newEvent: $event")
            if(event.hasActiveConnection){
                textView.text = "Connected to ${
                when(event.type) {
                    DATA -> "Mobile data"
                    WIFI -> "Wifi"
                    VPN -> "VPN"
                    else -> "Unknown network"
                }
                }"
            }else{
                textView.text = "No Network"
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // call this method when your done with Netware
        if(::registry.isInitialized)
            registry.unregister()
    }
}
