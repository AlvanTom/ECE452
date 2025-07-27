import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Initialize Firebase Admin
admin.initializeApp();

// Get Firestore instance
const db = admin.firestore();

// Example function - can be removed later
export const helloWorld = functions.https.onCall((body: any, context: any) => {
  console.log("helloWorld function called with data:", body.data);

  return {
    message: "Hello from Firebase Functions!",
    timestamp: new Date().toISOString(),
    receivedData: body.data,
    userId: context.auth?.uid || null,
  };
});

// Example function that interacts with Firestore
export const getUsers = functions.https.onCall(
  async (body: any, context: any) => {
    try {
      // Check if user is authenticated
      if (!context.auth) {
        throw new functions.https.HttpsError(
          "unauthenticated",
          "User must be authenticated"
        );
      }

      const usersSnapshot = await db.collection("users").get();
      const users = usersSnapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      return { users };
    } catch (error) {
      console.error("Error in getUsers:", error);
      throw new functions.https.HttpsError("internal", "Error fetching users");
    }
  }
);

export { createUser, updateUser } from "./user";
export {
  createSession,
  getSessionByID,
  getSessionsByUID,
  putSession,
} from "./session";
