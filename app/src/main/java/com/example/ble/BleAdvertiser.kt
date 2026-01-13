package com.example.ble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest
import java.util.UUID

class BleAdvertiser(private val context: Context) {  // creates class and saves the android env reference in context

    companion object {
        private const val TAG = "BLEAdvertiser"
        private const val ROTATION_WINDOW_MS = 5 * 60 * 1000L
        private const val MAX_MANUFACTURER_BYTES = 20
    }
    private val advertiser = run {
        val manager =
            context.getSystemService /* Demands Bluetooth service through the reference */(Context.BLUETOOTH_SERVICE) as? BluetoothManager /* Bluetooth Controller */
        manager?.adapter?.bluetoothLeAdvertiser /*  gets bluetooth adapter and advertiser through this object */
    }

    private var isAdvertising = false


    private val callback = object : AdvertiseCallback() { /* this object listens to advertising result */
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            isAdvertising = true
            Log.d(TAG, "BLE advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.e(TAG, "BLE advertising failed: $errorCode")
        }
    }


    // PUBLIC API


    @RequiresPermission( /* runtime permissions (does not contribute to the program only for your convenience ) */
        conditional = true,
        allOf = [
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    fun start() { /* func to start advertising */
        if (isAdvertising) return
        if (!isBluetoothEnabled()) return
        if (!isBleAdvertiseAllowed()) return /* permission check */
        if (advertiser == null) return /* stops if not granted */

        val advertiseData = AdvertiseData.Builder() /* creates a builder object */
            .addServiceUuid(  /* creates an unique identifier for our app */
                ParcelUuid(UUID.fromString(Constants.SERVICE_UUID))
            )
            .addManufacturerData( /* generates custom bytes of data to advertise */
                Constants.MANUFACTURER_ID,
                generateEphemeralId()
            )
            .build() /* build the object */

        val settings = AdvertiseSettings.Builder()
            // Use LOW_LATENCY only while foreground service is active
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) /* fast discovery but high battery usage */
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) /* strong signal as we need accuracy */
            .setConnectable(false) /* sets to broadcast only mode , no connection */
            .build()

        try {
            advertiser.startAdvertising(settings, advertiseData, callback) /* starts advertising */
            Log.d(TAG,"Advertising Started"  )
        } catch (e: SecurityException) {
           Log.e(TAG, "Security issue while starting advertising " , e)
        }
    }



    @RequiresPermission(
        conditional = true,
        allOf = [Manifest.permission.BLUETOOTH_ADVERTISE]
    )
    fun stop() { /* function to stop advertising */
        if (!isAdvertising) return
        if (advertiser == null) return

        try {
            advertiser.stopAdvertising(callback)
            isAdvertising = false
            Log.d(TAG , "BLE Advertising stopped ")
        } catch (e: SecurityException) {
            Log.e(TAG , "Security Issue while stopping Advertising", e)
        }
    }


    // INTERNAL HELPERS


/* permission check helper function */

    private fun isBluetoothEnabled(): Boolean {
        val manager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter?.isEnabled == true
    }

    private fun isBleAdvertiseAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { /* android 12 + only */
            ContextCompat.checkSelfPermission( /* checks runtime permissions */
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 12, advertise permission does not exist
            true
        }
    }

    // ROTATING ID , WINDOW - 5 MIN

    private fun generateEphemeralId(): ByteArray { /* func :  generates ephemeral id */
        val uid = FirebaseAuth.getInstance().uid ?: "" /* gets logged in user id empty string if not */
        val window = System.currentTimeMillis() / (5 * 60 * 1000)  /* changes every 5 min */

        return MessageDigest.getInstance("SHA-256") /* converts user + time into anonymous bytes */
            .digest("$uid$window".toByteArray())
    }
}
