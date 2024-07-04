# OK Gemini, what's for dinner?

This is the companion app for the talk titled `OK Gemini, what's for dinner?`
It is a Kotlin Multiplatform project targeting Android, iOS. It uses multimodal Gemini LLM access to provide a grocery list and a recipe from a photo.

Find the [slides](ok_gemini_slides.pdf) in the root directory/

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
2. Make the script `token` in the root directory excecutable `chmod 755 token`
3. Run the script `./token` It will use gcloud to print the token, copy it into local properties and rebuild the `BuildKonfig file`
4. Build and run the app
