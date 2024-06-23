This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

# How to setup the token for this app to work

You need a token to access the api

Prerequisites to run:
1. Download gcloud CLI (using brew gcloud command)
2. Run gcloud auth print-access-token
3. Copy the token into local.properties in the format below  
   `VERTEX_TOKEN=<insert your token here>`
4. Run the command `./gradlew generateBuildKonfig` to read the token from your local.properties an include it in the code
