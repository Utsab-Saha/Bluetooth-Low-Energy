const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
// hvjhkj
exports.onEncounter = functions.firestore
  .document("encounters/{id}")
  .onCreate(async (snap) => {

    const data = snap.data();

    // In production: deduplicate + match both users
    await admin.messaging().sendToTopic("global", {
      notification: {
        title: "Nearby User Detected",
        body: "Someone using the app is close to you"
      }
    });
  });
