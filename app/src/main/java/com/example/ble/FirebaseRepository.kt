package com.example.ble

import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    fun logEncounter(hash: ByteArray) {
        val uid = FirebaseAuth.getInstance().uid ?: return

        db.collection("encounters").add(
            mapOf(
                "user" to uid,
                "hash" to Base64.encodeToString(hash, Base64.NO_WRAP),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
    }
}
