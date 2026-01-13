package com.example.ble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import java.util.UUID

class BleScanner(
    private val context: Context,
    private val myUserId: String,
    private val repository: EncounterRepository
) {

    companion object {
        private const val TAG = "BleScanner"
    }

    private val scanner = run {
        val manager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter?.bluetoothLeScanner
    }

    private val callback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val manufacturerData =
                result.scanRecord?.manufacturerSpecificData ?: return

            if (manufacturerData.isNotEmpty()) {
                val payload = manufacturerData.valueAt(0)

                // Convert payload → user ID
                val nearbyUserId = payload.decodeToString()
                val rssi = result.rssi

                repository.recordEncounter(
                    myUserId = myUserId,
                    nearbyUserId = nearbyUserId,
                    rssi = rssi
                )

                Log.d(TAG, "Encounter detected: $nearbyUserId (RSSI=$rssi)")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed: $errorCode")
        }
    }

    // =========================
    // PUBLIC API
    // =========================

    @RequiresPermission(
        conditional = true,
        allOf = [
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    fun start() {
        if (!isBleScanAllowed()) return
        if (scanner == null) return

        val filter = ScanFilter.Builder()
            .setServiceUuid(
                ParcelUuid(UUID.fromString(Constants.SERVICE_UUID))
            )
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(listOf(filter), settings, callback)
            Log.d(TAG, "BLE scanning started")
        } catch (e: SecurityException) {
            Log.e(TAG, "BLE scanning failed", e)
        }
    }

    @RequiresPermission(
        conditional = true,
        allOf = [Manifest.permission.BLUETOOTH_SCAN]
    )
    fun stop() {
        if (scanner == null) return

        try {
            scanner.stopScan(callback)
            Log.d(TAG, "BLE scanning stopped")
        } catch (e: SecurityException) {
            Log.e(TAG, "Scan couldn't be stopped", e)
        }
    }

    // =========================
    // INTERNAL
    // =========================

    private fun isBleScanAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
