package com.example.ble

object BlePolicyProvider {
    fun forProfile(profile: OemProfile): BlePolicy {
        return when(profile) {
            OemProfile.GOOGLE -> BlePolicy(30000, 5000, 0, false, false)
            OemProfile.SAMSUNG -> BlePolicy(25000, 5000, 30000, true, false)
            OemProfile.XIAOMI -> BlePolicy(15000, 15000, 30000, true, true)
            OemProfile.VIVO -> BlePolicy(12000, 12000, 20000, true, true)
            OemProfile.OPPO_REALME -> BlePolicy(12000, 12000, 20000, true, true)
            OemProfile.ONEPLUS -> BlePolicy(20000, 8000, 0, true, false)
            else -> BlePolicy(10000, 15000, 20000, true, true)
        }
    }
}