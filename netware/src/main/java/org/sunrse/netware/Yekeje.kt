package org.sunrse.netware

// https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
// SingletonHolder for holding single Netware instance across application
open class Yekeje<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        // try avoid synchronization
        val ben = instance
        if (ben != null) {
            return ben
        }
        return synchronized(this) {
            val ben2 = instance
            if (ben2 != null) {
                ben2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}