# LifeCare - Android Application

An Android healthcare application built with Java and Firebase.

## Overview

LifeCare is an Android application designed to provide healthcare services and information to users.

## Technologies Used

- **Language**: Java
- **Build System**: Gradle with Kotlin DSL
- **UI Framework**: Android View System with View Binding
- **Backend**: Firebase (Authentication, Realtime Database)
- **Authentication**: Google Sign-In, Firebase Auth
- **Minimum SDK**: 34
- **Target SDK**: 34

## Features

- User authentication with Firebase
- Google Sign-In integration
- Real-time database connectivity
- Modern Android UI components

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 8 or higher
- Android SDK 34
- Google Services configuration file

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/sanjaymishra2002/lifecare.git
   cd lifecare
   ```

2. Open the project in Android Studio

3. Add your `google-services.json` file:
   - Download from Firebase Console
   - Place it in the `app/` directory

4. Sync Gradle and build the project:
   ```bash
   ./gradlew build
   ```

5. Run on an emulator or device:
   ```bash
   ./gradlew installDebug
   ```

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

The APK files will be generated in `app/build/outputs/apk/`

## Continuous Integration & Deployment

This repository includes GitHub Actions workflows for automated building and deployment.

### Automated Deployments

**Yes, any updates pushed to this repository will trigger automated workflows**, which can be configured to:
- Build the Android application
- Run tests
- Deploy to your server via webhooks

See [WEBHOOK_SETUP.md](WEBHOOK_SETUP.md) for detailed instructions on setting up automatic deployment to your server using Git webhooks.

## Project Structure

```
lifecare/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/lifecare/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── MainActivity2.java
│   │   │   │   ├── HomePage.java
│   │   │   │   └── SplashActivity.java
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/
├── .github/
│   └── workflows/
│       └── deploy.yml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is private and proprietary.

## Contact

Repository Owner: [@sanjaymishra2002](https://github.com/sanjaymishra2002)

## Additional Documentation

- [Webhook Setup Guide](WEBHOOK_SETUP.md) - How to configure automatic deployments
