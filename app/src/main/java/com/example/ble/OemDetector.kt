package com.example.ble

import android.os.Build

object OemDetector {
    fun detect(): OemProfile {
        val man = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return when {
            man.contains("google") -> OemProfile.GOOGLE
            man.contains("samsung") -> OemProfile.SAMSUNG
            man.contains("xiaomi") || brand.contains("redmi") -> OemProfile.XIAOMI
            man.contains("vivo") -> OemProfile.VIVO
            man.contains("oppo") || brand.contains("realme") -> OemProfile.OPPO_REALME
            man.contains("oneplus") -> OemProfile.ONEPLUS
            else -> OemProfile.UNKNOWN
        }
    }
}