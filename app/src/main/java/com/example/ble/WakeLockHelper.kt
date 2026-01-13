package com.example.ble

import android.content.Context
import android.os.PowerManager

object WakeLockHelper {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock(ctx: Context) {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ble:wl")
        wakeLock?.acquire(10 * 60 * 1000L)
    }
}
