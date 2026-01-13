package com.example.ble

data class BlePolicy(
    val scanDuration: Long,
    val restDuration: Long,
    val restartAdvertiseAfter: Long,
    val acquireWakeLock: Boolean,
    val needUserGuide: Boolean
)