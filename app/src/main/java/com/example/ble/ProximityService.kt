package com.example.ble

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import com.google.firebase.auth.FirebaseAuth

class ProximityService : Service() {

    private lateinit var advertiser: BleAdvertiser
    private lateinit var scanner: BleScanner
    private lateinit var repository: EncounterRepository

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        Log.d("PROXIMITY", "ProximityService created")

        // 🔐 Get logged-in user
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PROXIMITY", "No logged-in user → stopping service")
            stopSelf()
            return
        }

        repository = EncounterRepository()
        advertiser = BleAdvertiser(this)
        scanner = BleScanner(
            context = this,
            myUserId = userId,
            repository = repository
        )

        startForeground(
            Constants.FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.createForegroundNotification(this)
        )

        if (DevicePolicy.isAdvertisingRestricted()) {
            Log.d("PROXIMITY", "Restricted device detected → scan-only mode")
            startScanOnlyLoop()
        } else {
            Log.d("PROXIMITY", "Normal device → time-sliced BLE mode")
            startTimeSlicedLoop()
        }
    }

    // 🔁 SCAN ONLY (Vivo, Xiaomi, Oppo)
    private fun startScanOnlyLoop() {
        serviceScope.launch {
            while (isActive) {
                Log.d("PROXIMITY", "Scanning ON")
                scanner.start()

                delay(BleTiming.SCAN_WINDOW_MS)
                scanner.stop()
                Log.d("PROXIMITY", "Scanning OFF")

                delay(BleTiming.IDLE_WINDOW_MS + 2000)
            }
        }
    }

    // 🔁 ADVERTISE + SCAN (Pixel, Samsung)
    private fun startTimeSlicedLoop() {
        serviceScope.launch {
            while (isActive) {

                Log.d("PROXIMITY", "Advertising ON")
                advertiser.start()
                delay(BleTiming.ADVERTISE_WINDOW_MS)
                advertiser.stop()
                Log.d("PROXIMITY", "Advertising OFF")

                Log.d("PROXIMITY", "Scanning ON")
                scanner.start()
                delay(BleTiming.SCAN_WINDOW_MS)
                scanner.stop()
                Log.d("PROXIMITY", "Scanning OFF")

                delay(BleTiming.IDLE_WINDOW_MS)
            }
        }
    }

    override fun onDestroy() {
        Log.d("PROXIMITY", "ProximityService destroyed")

        serviceScope.cancel()
        advertiser.stop()
        scanner.stop()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

