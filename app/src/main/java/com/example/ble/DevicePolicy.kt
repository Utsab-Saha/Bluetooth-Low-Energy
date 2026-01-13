package com.example.ble

import android.os.Build

object DevicePolicy {

    fun isAdvertisingRestricted(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()

        return manufacturer.contains("vivo") ||
                manufacturer.contains("xiaomi") ||
                manufacturer.contains("oppo") ||
                manufacturer.contains("realme")
    }
}
