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
import java.util.concurrent.atomic.AtomicBoolean

class BleScanner(
    private val context: Context,
    private val myUserId: String,
    private val repository: EncounterRepository
) {

    companion object {
        private const val TAG = "BleScanner"
    }

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val scanner: BluetoothLeScanner? =
        bluetoothAdapter?.bluetoothLeScanner

    // 🔒 CRITICAL: prevents duplicate startScan()
    private val isScanning = AtomicBoolean(false)

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record = result.scanRecord ?: return
            val manufacturerData = record.manufacturerSpecificData

            if (manufacturerData == null || manufacturerData.size() == 0) return

            val payload = manufacturerData.valueAt(0)
            if (payload.isEmpty()) return

            val nearbyUserId = try {
                payload.decodeToString()
            } catch (e: Exception) {
                Log.w(TAG, "Invalid manufacturer payload")
                return
            }

            val rssi = result.rssi

            repository.recordEncounter(
                myUserId = myUserId,
                nearbyUserId = nearbyUserId,
                rssi = rssi
            )

            Log.d(TAG, "Encounter detected: $nearbyUserId (RSSI=$rssi)")
        }


        override fun onScanFailed(errorCode: Int) {
            isScanning.set(false)
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
        if (isScanning.get()) {
            Log.w(TAG, "start() ignored → already scanning")
            return
        }

        if (!isBleScanAllowed()) {
            Log.e(TAG, "BLE scan not allowed (permission)")
            return
        }

        if (scanner == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth unavailable or disabled")
            return
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(
                ParcelUuid(UUID.fromString(Constants.SERVICE_UUID))
            )
            .build()

        // ⚠️ LOW_LATENCY causes crashes on Vivo/Oppo
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        try {
            isScanning.set(true)
            scanner.startScan(listOf(filter), settings, scanCallback)
            Log.d(TAG, "BLE scanning started")
        } catch (e: SecurityException) {
            isScanning.set(false)
            Log.e(TAG, "Scan start blocked by OEM", e)
        } catch (e: Exception) {
            isScanning.set(false)
            Log.e(TAG, "Unexpected scan error", e)
        }
    }

    @RequiresPermission(
        conditional = true,
        allOf = [Manifest.permission.BLUETOOTH_SCAN]
    )
    fun stop() {
        if (!isScanning.get()) return
        if (scanner == null) return

        try {
            scanner.stopScan(scanCallback)
            Log.d(TAG, "BLE scanning stopped")
        } catch (e: Exception) {
            // OEMs sometimes throw here — ignore safely
            Log.w(TAG, "stopScan exception ignored", e)
        } finally {
            isScanning.set(false)
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
