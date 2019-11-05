package org.sunrse.netware.app

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import org.legobyte.netware.netware
import org.legobyte.netware.observe
import org.legobyte.netware.with

class MyNetwareCustomView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {

    init {
        netware with this observe {event->
            // deal with this event.
            // do not worry about memory-leak. netware will take care of that
        }
    }

}