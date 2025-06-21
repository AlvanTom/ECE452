const { initializeApp } = require('firebase/app');
const { getFunctions, httpsCallable, connectFunctionsEmulator } = require('firebase/functions');

// Firebase configuration
const firebaseConfig = {
  projectId: "climbr-9208c",
  apiKey: "fake-api-key-for-testing",
  authDomain: "climbr-9208c.firebaseapp.com",
  storageBucket: "climbr-9208c.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abcdef123456"
};

// Initialize Firebase app
const app = initializeApp(firebaseConfig);

// Get Functions instance
const functions = getFunctions(app);

// Connect to Functions emulator
connectFunctionsEmulator(functions, '127.0.0.1', 5001);

// Create callable function reference
const helloWorld = httpsCallable(functions, 'helloWorld');

// Call the function
async function testHelloWorld() {
  try {
    console.log('Calling helloWorld function...');
    const result = await helloWorld({ data: "hello from Node!" });
    console.log('Response:', result.data);
  } catch (error) {
    console.error('Error calling helloWorld function:', error);
  }
}

// Run the test
testHelloWorld(); 