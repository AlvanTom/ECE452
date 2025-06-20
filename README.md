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

### Backend

1. **Install Firebase CLI globally**: `npm install -g firebase-tools`
   - This installs the latest Firebase CLI globally on your system.
2. **Navigate to the functions folder**: `cd backend/functions`
3. **Install function dependencies**: `npm install`
4. **To run the Cloud Functions locally**, start the Firebase Emulator: `npm run serve`
5. **To deploy the Cloud Functions to Firebase**, run: `npm run deploy`

#### Testing Backend Locally with Postman

Ensure the local emulator is running.

1. **Configure Postman**:

   - Open Postman
   - Create a new request
   - Set the request method (POST)
   - Set the URL to: `http://localhost:5001/climbr-9208c/us-central1/FUNCTION_NAME`
   - Replace the name of the function (e.g. helloWorld)

2. **Example Request**:

   ```
   Method: POST
   URL: http://localhost:5001/climbr-9208c/us-central1/helloWorld
   Headers:
     Content-Type: application/json
   Body (raw JSON):
   {
     "data": "test message"
   }
   ```

3. **Send the request** and check the response in Postman
