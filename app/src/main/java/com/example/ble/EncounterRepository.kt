package com.example.ble

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EncounterRepository {

    private val db = FirebaseFirestore.getInstance()

    fun recordEncounter(
        myUserId: String,
        nearbyUserId: String,
        rssi: Int
    ) {
        val confidence = when {
            rssi > -60 -> "HIGH"
            rssi > -80 -> "MEDIUM"
            else -> "LOW"
        }

        val data = hashMapOf(
            "from" to myUserId,
            "to" to nearbyUserId,
            "timestamp" to FieldValue.serverTimestamp(),
            "source" to "BLE",
            "rssi" to rssi,
            "confidence" to confidence
        )

        db.collection("ble_encounters")
            .add(data)
            .addOnFailureListener {
                // Optional: log or retry later
            }
    }
}
