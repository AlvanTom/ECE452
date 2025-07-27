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

// Returns all verified posts in the database
export const getFeed = functions.https.onCall(async (request) => {
    // Check if user is authenticated
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const userRef = db.collection("users").doc("9R0NeLXu6fY7ziTyQH7Cw6UhWPh1");
  const userDoc = await userRef.get();

  if (!userDoc.exists) {
    return { posts: [] };
  }

  const postsSnapshot = await db
    .collection("posts")
    .where("userId", "==", "9R0NeLXu6fY7ziTyQH7Cw6UhWPh1")
    .get();

  const postsData = postsSnapshot.docs.map((doc) => ({
    id: doc.id,        
    ...doc.data()
  }));
  return { posts: postsData };
});
