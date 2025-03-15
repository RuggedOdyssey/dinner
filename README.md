# OK Gemini, what's for dinner? - Project Guidelines

## Project Overview

"OK Gemini, what's for dinner?" is a Kotlin Multiplatform application that demonstrates the capabilities of Google's Gemini multimodal Large Language Model (LLM). The app allows users to take photos of ingredients and receive suggested recipes and grocery lists based on the image content.

This is the companion app for the talk titled "OK Gemini, what's for dinner?" and for the talk "Dinner for two with Gemini". Find the [ok gemini slides](ok_gemini_slides.pdf) and [dinner for two slides](dinner_for_two.pdf)vin the root directory.

### Key Features
- Photo-to-recipe conversion using Gemini LLM
- Grocery list generation based on image analysis
- Cross-platform support for Android and iOS

### Technology Stack
- **Kotlin Multiplatform**: For shared code across platforms
- **Compose Multiplatform**: For UI components
- **Google Gemini API**: For multimodal LLM capabilities

> ℹ️ **Info**: The iOS app needs some attention it doesn't run at the moment.

- **SwiftUI**: For iOS-specific UI components

## Project Structure

The project follows the standard Kotlin Multiplatform structure:

- `/composeApp`: Contains code shared across platforms
  - `commonMain`: Common code for all targets
  - Platform-specific folders for platform-specific implementations

- `/iosApp`: iOS application entry point and SwiftUI code

## Development Setup

### Prerequisites
1. Android Studio or IntelliJ IDEA with Kotlin Multiplatform support
2. Xcode (for iOS development)
3. Google Cloud CLI (for API token generation)

### API Token Setup
To access the Gemini API, you need to set up a token:
1. Download gcloud CLI (using `brew gcloud` command)
2. Make the script `token` in the root directory executable: `chmod 755 token`
3. Run the script `./token` - it will use gcloud to print the token, copy it into local properties, and rebuild the `BuildKonfig` file
4. Build and run the app

## Contribution Guidelines

When contributing to this project, please follow these guidelines:

1. Create feature branches from the main branch
2. Follow Kotlin coding conventions
3. Write unit tests for new functionality
4. Update documentation as needed
5. Submit pull requests for review

## Resources

- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Google Gemini API Documentation](https://ai.google.dev/docs)