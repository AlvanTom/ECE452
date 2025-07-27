import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Get Firestore instance
const db = admin.firestore();

export const createPost = functions.https.onCall(async (request) => {
  // Check if user is authenticated
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const {
    uid,
    title,
    location,
    isIndoor,
    date,
    vScale,
    notes,
  }: {
    uid: string;
    title: string;
    location: string;
    isIndoor: boolean;
    date: string;
    vScale: number;
    notes?: string;
  } = request.data;

  console.log("Parsed data:", { uid, title, location, isIndoor, date, vScale, notes });

  if (!uid || !title || !location || typeof isIndoor !== "boolean") {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing/Invalid fields: (uid, title, location, isIndoor)"
    );
  }

  const postDoc = {
    userId: uid,
    title,
    location,
    isIndoor,
    date,
    difficulty: vScale, // Store as difficulty in database
    notes,
  };

  const postRef = await db.collection("posts").add(postDoc);

  return { postId: postRef.id };

});