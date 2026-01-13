package com.example.ble


/**
 * Central place for all app-wide constants.
 * Keeping these here avoids magic numbers spread across BLE / Firebase code.
 */
object Constants {

    /* ========= BLE CONFIG ========= */

    // Custom service UUID used to filter BLE advertisements
    const val SERVICE_UUID = "0000FEAA-0000-1000-8000-00805F9B34FB"

    // Arbitrary manufacturer ID (must be consistent across advertiser & scanner)
    const val MANUFACTURER_ID = 0x1234


    /* ========= EPHEMERAL ID CONFIG ========= */

    // Time window (in minutes) after which BLE IDs rotate
    const val EPHEMERAL_WINDOW_MINUTES = 5L


    /* ========= FOREGROUND SERVICE ========= */

    const val FOREGROUND_NOTIFICATION_ID = 1001
    const val FOREGROUND_CHANNEL_ID = "proximity_service_channel"
    const val FOREGROUND_CHANNEL_NAME = "BLE Proximity Service"


    /* ========= FIREBASE ========= */

    const val FIRESTORE_ENCOUNTERS_COLLECTION = "encounters"


    /* ========= NOTIFICATION ========= */

    const val NOTIFICATION_TITLE = "Nearby User Detected"
    const val NOTIFICATION_BODY = "Someone using the app is close to you"
}
