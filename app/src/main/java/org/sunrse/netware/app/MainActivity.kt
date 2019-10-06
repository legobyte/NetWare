package org.sunrse.netware.app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import androidx.core.os.HandlerCompat.postDelayed
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.sunrse.netware.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(isConnectedToNetwork){
            if(isWifiConnection){

            }
            if(isCellularConnection){

            }
        }else{
            //
        }

        Netware.getInstance(this).observe(this, Observer {event->

        })
        netware.observe(this, Observer {event->

        })
        netware with this observe {
            Log.i("MainActivityNet", "NetWare type: ${it.type}, state: ${it.state} ${this@MainActivity.toString()}")
        }

        Handler().let {
            val parent = tv.parent as ViewGroup
            it.postDelayed({
                parent.removeView(tv)
            }, 4_000)

            it.postDelayed({
                parent.addView(tv)
            }, 10_000)
        }



    }
}
