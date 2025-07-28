import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Get Firestore instance
const db = admin.firestore();

export const createPost = functions.https.onCall(async (request) => {
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in to post"
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
    mediaUrls,
  }: {
    uid: string;
    title: string;
    location: string;
    isIndoor: boolean;
    date: string;
    vScale: number;
    notes?: string;
    mediaUrls?: string[];
  } = request.data;

  // Fetch displayName and profilePhotoUrl from users collection
  const userRef = admin.firestore().collection("users").doc(uid);
  const userDoc = await userRef.get();
  if (!userDoc.exists) {
    throw new functions.https.HttpsError(
      "not-found",
      "User profile not found"
    );
  }
  const userData = userDoc.data();
  const username = userData?.displayName || "Unknown";
  const userProfileImage = userData?.profilePhotoUrl || null;

  const postDoc = {
    userId: uid,
    username, // Store display name in post
    userProfileImage, // Store profile photo URL in post
    title,
    location,
    isIndoor,
    date,
    difficulty: vScale,
    notes,
    mediaUrls: mediaUrls || [],
    likes: 0,
    createdAt: new Date().toISOString(),
  };

  const postRef = await db.collection("posts").add(postDoc);

  return { postId: postRef.id };
});

// true to like, false to unlike
export const toggleLike = functions.https.onCall(async (request) => {
  // Auth check
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "You must be logged in to like a post"
    );
  }

  const {
    postId,
    increment,
  }: {
    postId: string;
    increment: boolean;
  } = request.data;

  if (!postId || typeof increment !== "boolean") {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing or invalid fields (postId, increment)"
    );
  }

  const postRef = db.collection("posts").doc(postId);

  await db.runTransaction(async (transaction) => {
    const postSnap = await transaction.get(postRef);

    if (!postSnap.exists) {
      throw new functions.https.HttpsError("not-found", "Post not found");
    }

    const currentLikes = postSnap.data()?.likes ?? 0;
    const newLikes = increment
      ? currentLikes + 1
      : Math.max(currentLikes - 1, 0);

    transaction.update(postRef, { likes: newLikes });
  });

  return { success: true };
});

// Returns all verified posts in the database
export const getFeed = functions.https.onCall(async (request) => {
  // Auth check commented for testing
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const userId = "9R0NeLXu6fY7ziTyQH7Cw6UhWPh1";
  const userRef = db.collection("users").doc(userId);
  const userDoc = await userRef.get();

  if (!userDoc.exists) {
    return { posts: [] };
  }

  const postsSnapshot = await db
    .collection("posts")
    .where("userId", "==", userId)
    .get();

  // Fetch comments for each post
  const postsData = await Promise.all(
    postsSnapshot.docs.map(async (doc) => {
      const postId = doc.id;
      const postData = doc.data();

      const commentsSnapshot = await db
        .collection("posts")
        .doc(postId)
        .collection("comments")
        .orderBy("createdAt", "asc")
        .get();

      const comments = commentsSnapshot.docs.map((commentDoc) => ({
        id: commentDoc.id,
        ...commentDoc.data(),
      }));

      return {
        id: postId,
        ...postData,
        comments,
      };
    })
  );

  return { posts: postsData };
});
