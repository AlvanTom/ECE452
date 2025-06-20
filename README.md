# ECE452 Project

This repository contains both the Android application and Firebase backend for the ECE452 project.

## Project Structure

```
ECE452/
├── android/          # Android application
│   ├── app/         # Main Android app module
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
└── backend/         # Firebase Cloud Functions backend
```

## Firebase Services

This project uses the following Firebase services:

- **Cloud Firestore**: Serverless database backend
- **Firebase Cloud Functions**: Backend logic and API endpoints
- **Firebase Authentication**: User authentication and management

## Setup Instructions

### Android App

1. Navigate to the `android` directory
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the application

### Backend (Firebase Cloud Functions)

1. Navigate to the `backend` directory
2. Install Firebase CLI: `npm install -g firebase-tools`
3. Login to Firebase: `firebase login`
4. Initialize Firebase Functions: `firebase init functions`
5. Deploy functions: `firebase deploy --only functions`

## Development

- Android development: Work in the `android/` directory
- Backend development: Work in the `backend/` directory
- Both can be developed independently but share the same Firebase project
