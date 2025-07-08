import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Get Firestore instance
const db = admin.firestore();

export const createSession = functions.https.onCall(async (request) => {
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
    gymName,
    routes,
  }: {
    uid: string;
    title: string;
    location: string;
    isIndoor: boolean;
    gymName?: string;
    routes: {
      routeName: string;
      difficulty: string;
      tags: string[];
      notes?: string;
      attempts: { success: boolean; createdAt: string }[]; // timestamp from FE
    }[];
  } = request.data;

  if (!uid || !title || !location || typeof isIndoor !== "boolean") {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing/Invalid fields: uid, title, location, isIndoor"
    );
  }
  if (!Array.isArray(routes)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Routes must be an array"
    );
  }

  const sessionDoc = {
    userId: uid,
    title,
    location,
    isIndoor,
    gymName: gymName ?? null,
    createdAt: new Date().toISOString(),
  };

  const sessionRef = await db.collection("sessions").add(sessionDoc);

  for (const route of routes) {
    if (
      !route.routeName ||
      !route.difficulty ||
      !Array.isArray(route.tags) ||
      !Array.isArray(route.attempts)
    ) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Missing/Invalid fields: routeName, difficulty, tags, attempts"
      );
    }

    const { routeName, difficulty, tags, notes, attempts } = route;

    const routeRef = await sessionRef.collection("routes").add({
      routeName,
      difficulty,
      tags,
      notes: notes ?? null,
    });

    for (const attempt of attempts) {
      if (typeof attempt.success !== "boolean" || !attempt.createdAt) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Missing/Invalid fields: success, createdAt"
        );
      }
      await routeRef.collection("attempts").add({
        success: attempt.success,
        createdAt: attempt.createdAt,
      });
    }
  }

  return { sessionId: sessionRef.id };
});

export const getSessionByID = functions.https.onCall(async (request) => {
  // Check if user is authenticated
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const { sessionId }: { sessionId: string } = request.data;

  if (!sessionId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Session ID is required"
    );
  }

  const sessionRef = db.collection("sessions").doc(sessionId);
  const sessionDoc = await sessionRef.get();

  if (!sessionDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Session not found");
  }

  const sessionData = sessionDoc.data();

  const routesDoc = await sessionRef.collection("routes").get();
  const routesData = await Promise.all(
    routesDoc.docs.map(async (doc) => {
      const routeData = doc.data();

      // Get attempts for this route
      const attemptsDoc = await doc.ref.collection("attempts").get();
      const attemptsData = attemptsDoc.docs.map((attemptDoc) =>
        attemptDoc.data()
      );

      return {
        id: doc.id,
        ...routeData,
        attempts: attemptsData,
      };
    })
  );

  return { sessionId, sessionData, routesData };
});

export const getSessionsByUID = functions.https.onCall(async (request) => {
  // Check if user is authenticated
  if (!request.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in"
    );
  }

  const { uid }: { uid: string } = request.data;

  if (!uid) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "User ID is required"
    );
  }

  const userRef = db.collection("users").doc(uid);
  const userDoc = await userRef.get();

  if (!userDoc.exists) {
    throw new functions.https.HttpsError("not-found", "User not found");
  }

  const sessionsRef = db.collection("sessions").where("userId", "==", uid);
  const sessionsDoc = await sessionsRef.get();

  const sessionsData = sessionsDoc.docs.map((doc) => doc.id);
  return { sessionIds: sessionsData };
});
