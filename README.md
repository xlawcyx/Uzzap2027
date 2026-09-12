<div align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_uzzap_logo.jpg" alt="Uzzap logo" width="128" />
  <h1>Uzzap</h1>
  <p><strong>A modern Android instant messenger for conversations, communities, and connections.</strong></p>
  <p>
    <a href="https://developer.android.com/"><img src="https://img.shields.io/badge/Android-API_24%2B-3DDC84?logo=android&amp;logoColor=white" alt="Android API 24+" /></a>
    <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&amp;logoColor=white" alt="Kotlin 2.2.10" /></a>
    <a href="https://developer.android.com/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&amp;logoColor=white" alt="Jetpack Compose Material 3" /></a>
    <a href="https://firebase.google.com/"><img src="https://img.shields.io/badge/Firebase-Auth_%26_Firestore-FFCA28?logo=firebase&amp;logoColor=black" alt="Firebase Authentication and Firestore" /></a>
  </p>
</div>

---

Uzzap brings buddy lists, direct messaging, and Philippine regional chatrooms into one Compose-powered Android experience. Users can manage their profile and presence, join communities, and keep conversations synced through Firebase.

## ✨ Features

| Connect | Converse | Personalize |
| --- | --- | --- |
| Buddy lists and requests | Direct and regional room chats | Profiles and presence controls |
| Search and category filters | Reply, buzz, and reporting tools | Notification and sound preferences |
| Online status indicators | Local history with Room | Light and dark themes |

## 🧰 Tech stack

| Area | Technology |
| --- | --- |
| UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | ViewModel, StateFlow, repository pattern |
| Local data | Room |
| Cloud | Firebase Authentication and Cloud Firestore |
| Build | Gradle 9.3.1, Android Gradle Plugin 9.1.1 |
| Tests | JUnit, Robolectric, Compose UI Test, Roborazzi |

## 🚀 Getting started

### Requirements

- JDK 21
- Android SDK Platform 36 and Build Tools 36.x
- Gradle 9.3.1 or Android Studio with an equivalent Gradle setup
- An Android device or emulator running API 24 or later

> [!NOTE]
> The repository currently includes Gradle wrapper properties but not the `gradlew` launcher or wrapper JAR. Use a locally installed Gradle 9.3.1 until those files are restored.

### Configure Firebase

Firebase-backed sign-in and messaging need an Android Firebase project:

1. Register the application ID `com.aistudio.uzzap.kxvtpm` in Firebase.
2. Download the project's `google-services.json`.
3. Place it at `app/google-services.json`.
4. Enable Authentication and Cloud Firestore.

Enable the Firebase Authentication **Email/Password** provider. Uzzap maps normalized usernames
to internal Firebase Auth email identifiers; passwords are handled by Firebase Authentication and
must never be stored in Firestore.

Before deploying [`firestore.rules`](firestore.rules) over an existing project, migrate legacy
accounts with an administrator-controlled script:

- create a Firebase Authentication user for each account;
- add its `authUid` to the private `users/{username}` record;
- copy only public presence fields into `public_profiles/{username}`;
- remove the legacy `password` field from Firestore.

The checked-in rules intentionally deny legacy records without ownership metadata. Do not weaken
the rules to perform this migration from an Android client.

> [!IMPORTANT]
> Keep `google-services.json` and production credentials out of version control. The debug APK compiles without this file, but Firebase features will not work correctly at runtime.

### Build and run

```bash
gradle :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Install it through Android Studio or with ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

<details>
<summary><strong>Building on a memory-constrained machine</strong></summary>

```bash
gradle --no-daemon --no-configuration-cache \
  -Dorg.gradle.jvmargs='-Xmx2g -Dfile.encoding=UTF-8' \
  -Dorg.gradle.workers.max=1 \
  -Dorg.gradle.parallel=false \
  :app:assembleDebug
```

</details>

## 🧪 Testing

Run host-side unit and Robolectric tests:

```bash
gradle --no-configuration-cache :app:testDebugUnitTest
```

Run instrumentation tests with a connected device or emulator:

```bash
gradle :app:connectedDebugAndroidTest
```

## 🗂️ Project structure

```text
app/src/main/java/com/example/
├── auth/           Authentication integration
├── data/           Models, Room storage, and Firestore services
├── ui/components/  Shared Compose components
├── ui/screens/     Application screens
├── ui/theme/       Colors, typography, and app theme
├── ui/viewmodel/   UI state and application actions
└── MainActivity.kt Application entry point and top-level navigation
```

Firestore security rules and indexes live in [`firestore.rules`](firestore.rules) and [`firestore.indexes.json`](firestore.indexes.json).
