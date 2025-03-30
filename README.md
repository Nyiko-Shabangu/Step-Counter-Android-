# Step Counter Android Application

## Overview
The Step Counter app is a fitness tracking application that monitors and records a user's step count using the device's built-in step counter sensor. The app provides real-time step count updates, local storage for tracking progress, and synchronization with Firebase and a custom API for data backup and analysis.

## Features
- **Real-time Step Tracking**: Utilizes the device's step counter sensor to accurately track steps
- **Persistent Storage**: Saves step count data in a local database for offline access
- **Cloud Synchronization**: Automatically syncs step data with Firebase Realtime Database
- **API Integration**: Posts step count data to a custom backend API
- **User-friendly Interface**: Simple display of current and historical step counts

## Technical Components

### Sensors
- Utilizes Android's `SensorManager` to access the `TYPE_STEP_COUNTER` sensor
- Implements `SensorEventListener` for real-time step count updates

### Data Storage
- Local storage through Room Database
- Remote storage through Firebase Realtime Database
- API integration for advanced data processing

### Permissions
- Handles the `ACTIVITY_RECOGNITION` permission for Android 10 (Q) and above
- Implements proper permission request flow with user guidance

## Project Structure

### Key Files
- `MainActivity.kt`: Main application entry point and sensor handling
- `StepCount.kt`: Data model for step count entries
- `StepCountDao.kt`: Data Access Object for local database operations
- `StepCountDatabase.kt`: Room database configuration
- `RetrofitInstance.kt`: API client setup for remote data synchronization
- `StepCountApiService.kt`: API endpoint definitions

### Architecture
The application follows a modern Android architecture approach:
- Kotlin as the primary language
- Jetpack Compose for UI components
- Room for local database management
- Retrofit for API communication
- Firebase for cloud storage
- Coroutines for asynchronous operations

## Setup and Installation

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Google Firebase account
- Backend API (optional)

### Firebase Setup
1. Create a new Firebase project
2. Add your Android application to the Firebase project
3. Download the `google-services.json` file and place it in the app module
4. Enable the Realtime Database in your Firebase console

### API Configuration
1. Update the base URL in `RetrofitInstance.kt` to point to your backend API
2. Ensure your API endpoints match the interface defined in `StepCountApiService.kt`

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on a physical device (emulators may not have step counter sensors)

## Usage
1. Launch the app
2. Grant activity recognition permission when prompted
3. Start walking to see the step counter increase
4. The app will automatically save your step counts locally and sync with Firebase
5. View your step history in the main interface

## Troubleshooting
- **Sensor Not Available**: Some devices may not have a step counter sensor. The app will display a message if this is the case.
- **Permission Denied**: If the user denies the activity recognition permission, step counting will not function properly.
- **API Connection Issues**: Check your internet connection and API endpoint configuration if synchronization fails.

## Future Enhancements
- Step goal setting and achievements
- Advanced statistics and visualizations
- User accounts and profiles
- Social sharing features
- Integration with other fitness metrics (calories, distance)
- Support for wearable devices
