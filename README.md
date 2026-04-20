# New You

New You is a wearable health monitoring system consisting of a Wear OS smartwatch application and an Android mobile application. The system collects, stores, and analyzes fitness and health data to help users track their daily activity and improve their long-term physical health.

This project is useful because it consolidates fragmented health data into a single platform, allowing users to monitor activity levels, compare historical performance, and receive recommendations for improving their health.

---

## Features

### Core Features (MVP)
- Connect smartwatch to smartphone
- Start and stop workouts from smartwatch
- Select workouts (Running, Swimming, Biking)
- Track health data:
  - Heart rate
  - Step count
  - Calories burned
  - Distance traveled
  - Activity minutes
  - Elevation
- Store health data locally using SQLite
- View health recommendations on mobile app

### Additional Features (In Progress)
- Graphs for health statistics
- Historical data visualization
- Health predictions
- Weekly/monthly summaries
- Notifications for low activity levels
- Expanded workout options (Yoga, Hiking, Strength Training, etc.)

---

## Technologies

### Languages & Tools
- Java
- Android Studio
- Android SDK
- Wear OS SDK

### APIs
- Wearable Data Layer API
- Android Sensor API
- Android Location API

### Database
- SQLite (local storage for health data)

---

## Installation

### Requirements
- Android Studio installed
- Android device or emulator (Android 10+)
- Wear OS device or emulator

### Steps
1. Clone the repository:
   git clone https://github.com/yourusername/new-you.git

2. Open the project in Android Studio

3. Build the project using Gradle

4. Run the mobile application on an Android device/emulator

5. Run the Wear OS app on a smartwatch/emulator

6. Connect the smartwatch to the mobile device

7. Launch both apps and begin tracking workouts

---

## Development Setup

This section is for developers who want to contribute or build the project.

### Environment Setup
- Install Android Studio
- Ensure Android SDK and Wear OS SDK are installed
- Set up an Android emulator and Wear OS emulator (or use physical devices)

### Build Instructions
1. Open project in Android Studio
2. Sync Gradle files
3. Build project using:
   Build > Make Project
4. Run mobile app configuration
5. Run Wear OS configuration

### Project Structure
/mobile           -> Android application  
/wear             -> Wear OS application  
/database         -> SQLite database logic  
/gradle -> Gradle wrapper configuration files

### Branching Strategy
- main → stable releases  
- development → active development  
- feature/* → individual features  

---

## License

This project is licensed under the MIT License.

You are free to use, modify, and distribute this software with proper attribution.

---

## Contributors

- Aiden Liriano (Project Developer)

---

## Project Status

Current Status: Alpha

The core functionality (data tracking, smartwatch connection, and storage) is implemented. Additional features such as data visualization, predictions, and expanded workouts are in development.

---

## Known Issues

- Limited device testing (primarily tested on one smartwatch model)
- No cloud backup (local storage only)
- UI may not be fully optimized for all screen sizes

---

## Roadmap

### Upcoming Features
- Graph-based data visualization
- Weekly/monthly health summaries
- Push notifications for inactivity
- Expanded workout library

### Future (Version 2.0)
- Cloud data backup
- Cross-device syncing
- AI-based health recommendations
- Social fitness features
- iOS companion app

---

## Support

For questions or support, contact:

Aiden Liriano  
amliriano@student.fullsail.edu

---

## Additional Notes

- Data is stored locally using SQLite for performance and simplicity
- Designed as an educational project focusing on wearable-to-mobile communication
