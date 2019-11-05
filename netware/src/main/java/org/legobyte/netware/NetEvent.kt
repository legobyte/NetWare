package org.legobyte.netware

import androidx.annotation.IntDef

typealias NetEventListener = (event:NetEvent) -> Unit

// types
const val DATA = 1
const val WIFI = 2
const val VPN = 3
const val OTHERS = 4

// states
const val DISCONNECTING = 0
const val DISCONNECTED = 1
const val CONNECTING = 2
const val CONNECTED = 3
const val SUSPENDED = 4
const val UNKNOWN = 5
// this is for internal use only
internal const val NO_STATE = -1

// Network Connectivity Change Event
data class NetEvent(
    @NetType
    val type:Int,
    @State
    val state:Int
){
    // returns true if this is an event of ActiveConnection
    val hasActiveConnection:Boolean
        get() = intArrayOf(CONNECTING, CONNECTED).contains(state)

    // returns true if this is an event of InactiveConnection
    val isDisconnectedOrSuspended:Boolean
        get() = intArrayOf(DISCONNECTING, DISCONNECTED, SUSPENDED).contains(state)
}


@IntDef(CONNECTED, CONNECTING, DISCONNECTING, DISCONNECTED, UNKNOWN, SUSPENDED)
annotation class State

@IntDef(DATA, WIFI, VPN, OTHERS)
annotation class NetType