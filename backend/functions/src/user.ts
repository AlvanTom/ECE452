import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();

export const createUser = functions.https.onCall(
  async (data: any, context: any) => {
    // Check if the user is authenticated
    if (!context.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated to create a user profile"
      );
    }

    const { displayName, email }: { displayName: string; email: string } =
      data.data;
    const userId = context.auth.uid;

    if (!displayName) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Display name is required"
      );
    }

    if (!email) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Email is required"
      );
    }

    try {
      // Create timestamps
      const now = admin.firestore.Timestamp.now();

      // Create user document
      const userData = {
        displayName: displayName,
        email: email,
        createdAt: now,
        updatedAt: now,
      };

      // Add document to users collection
      await db.collection("users").doc(userId).set(userData);

      return {
        success: true,
        message: "User created successfully",
        userId: userId,
        userData: userData,
      };
    } catch (error) {
      console.error("Error creating user:", error);
      throw new functions.https.HttpsError("internal", "Failed to create user");
    }
  }
);
