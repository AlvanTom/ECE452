import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Get Firestore instance
const db = admin.firestore();

export const createSession = functions.https.onCall(async (data) => {
    const {
      uid,
      title,
      location,
      isIndoor,
      gymName,
      routes
    }: { 
      uid: string; 
      title: string; 
      location: string; 
      isIndoor: boolean; 
      gymName?: string 
      routes: {
        routeName: string;
        difficulty: string;
        tags: string[];
        notes?: string;
        attempts: { success: boolean; createdAt: string }[]; // timestamp from FE
      }[];
    } = data.data;
    
    if (!uid || !title || !location || typeof isIndoor !== 'boolean') {
      throw new functions.https.HttpsError('invalid-argument', 'Missing/Invalid fields: uid, title, location, isIndoor');
    }
    if (!Array.isArray(routes)) {
      throw new functions.https.HttpsError('invalid-argument', 'Routes must be an array');
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
        if (!route.routeName || !route.difficulty || !Array.isArray(route.tags) || !Array.isArray(route.attempts)) {
            throw new functions.https.HttpsError('invalid-argument', 'Missing/Invalid fields: routeName, difficulty, tags, attempts');
        }
        
        const { routeName, difficulty, tags, notes, attempts } = route;
    
        const routeRef = await sessionRef.collection("routes").add({
          routeName,
          difficulty,
          tags,
          notes: notes ?? null,
        });
    
        for (const attempt of attempts) {
            if (typeof attempt.success !== 'boolean' || !attempt.createdAt) {
                throw new functions.https.HttpsError('invalid-argument', 'Missing/Invalid fields: success, createdAt');
            }
            await routeRef.collection("attempts").add({
                success: attempt.success,
                createdAt: attempt.createdAt
            });
        }
        
      }
  
    return { sessionId: sessionRef.id };
  
  });

export const getSessionByID = functions.https.onCall(async (data) => {
    const { sessionId }: { sessionId: string } = data.data;
    
    if (!sessionId) {
        throw new functions.https.HttpsError('invalid-argument', 'Session ID is required');
    }
    
    const sessionRef = db.collection("sessions").doc(sessionId);
    const sessionDoc = await sessionRef.get();

    if (!sessionDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Session not found');
    }

    const sessionData = sessionDoc.data();

    const routesDoc = await sessionRef.collection("routes").get();
    const routesData = routesDoc.docs.map((doc) => doc.data());

    return { sessionId, sessionData, routesData };
});