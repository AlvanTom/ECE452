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
    } = data as unknown as { 
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
    };
    
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
    const routesData = await Promise.all(routesDoc.docs.map(async (doc) => {
        const routeData = doc.data();
        
        // Get attempts for this route
        const attemptsDoc = await doc.ref.collection("attempts").get();
        const attemptsData = attemptsDoc.docs.map(attemptDoc => attemptDoc.data());
        
        return {
            id: doc.id,
            ...routeData,
            attempts: attemptsData
        };
    }));

    return { sessionId, sessionData, routesData };
});

export const getUserSessions = functions.https.onCall(async (data) => {
    const { uid }: { uid: string } = data.data;
    
    if (!uid) {
        throw new functions.https.HttpsError('invalid-argument', 'User ID is required');
    }
    
    try {
        const sessionsSnapshot = await db.collection("sessions")
            .where("userId", "==", uid)
            .orderBy("createdAt", "desc")
            .get();
        
        const sessions = await Promise.all(sessionsSnapshot.docs.map(async (doc) => {
            const sessionData = doc.data();
            
            // Get routes count for this session
            const routesSnapshot = await doc.ref.collection("routes").get();
            
            return {
                id: doc.id,
                ...sessionData,
                routesCount: routesSnapshot.size
            };
        }));
        
        return { sessions };
    } catch (error) {
        console.error("Error fetching user sessions:", error);
        throw new functions.https.HttpsError("internal", "Error fetching user sessions");
    }
});

export const getActiveSessions = functions.https.onCall(async (data) => {
    const { uid }: { uid: string } = data.data;
    
    if (!uid) {
        throw new functions.https.HttpsError('invalid-argument', 'User ID is required');
    }
    
    try {
        // For now, we'll consider sessions created in the last 24 hours as "active"
        // You can modify this logic based on your requirements
        const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
        
        const activeSessionsSnapshot = await db.collection("sessions")
            .where("userId", "==", uid)
            .where("createdAt", ">=", twentyFourHoursAgo)
            .orderBy("createdAt", "desc")
            .get();
        
        const activeSessions = await Promise.all(activeSessionsSnapshot.docs.map(async (doc) => {
            const sessionData = doc.data();
            
            // Get routes for this session
            const routesSnapshot = await doc.ref.collection("routes").get();
            const routes = await Promise.all(routesSnapshot.docs.map(async (routeDoc) => {
                const routeData = routeDoc.data();
                
                // Get attempts for this route
                const attemptsSnapshot = await routeDoc.ref.collection("attempts").get();
                const attempts = attemptsSnapshot.docs.map(attemptDoc => attemptDoc.data());
                
                return {
                    id: routeDoc.id,
                    ...routeData,
                    attempts
                };
            }));
            
            return {
                id: doc.id,
                ...sessionData,
                routes
            };
        }));
        
        return { activeSessions };
    } catch (error) {
        console.error("Error fetching active sessions:", error);
        throw new functions.https.HttpsError("internal", "Error fetching active sessions");
    }
});