import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Get Firestore instance
const db = admin.firestore();

export const createPost = functions.https.onCall(async (request) => {
  // Check if user is authenticated
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

  console.log("Parsed data:", {
    uid,
    title,
    location,
    isIndoor,
    date,
    vScale,
    notes,
    mediaUrls,
  });

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
    mediaUrls: mediaUrls || [],
    likes: 0,
    createdAt: new Date().toISOString(),
  };

  const postRef = await db.collection("posts").add(postDoc);

  return { postId: postRef.id };
});

export const addComment = functions.https.onCall(async (request) => {
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "You must be logged in to comment"
    );
  }

  const {
    postId,
    content,
    username,
    userProfileImage,
  }: {
    postId: string;
    content: string;
    username: string;
    userProfileImage?: string;
  } = request.data;

  // const userId = request.auth.uid;

  if (!postId || !content || !username) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing required fields (postId, content, username)"
    );
  }

  const userId = "test-user-123"; // ← hardcoded for local testing

  const commentRef = db
    .collection("posts")
    .doc(postId)
    .collection("comments")
    .doc();

  const commentDoc = {
    id: commentRef.id,
    postId,
    userId,
    username,
    userProfileImage: userProfileImage || null,
    content,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  await commentRef.set(commentDoc);

  return { success: true, commentId: commentRef.id };
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
