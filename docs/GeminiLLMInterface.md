# Gemini LLM Interface Documentation

This document explains the parameters and interface needed to fetch responses from the Google Gemini Large Language Model (LLM) in the "OK Gemini, what's for dinner?" application.

## Overview

The application uses Google's Gemini multimodal LLM to generate recipe suggestions and grocery lists based on images of food and a list of available ingredients. The interaction with the Gemini LLM is handled through the Vertex AI API.

## Key Components

### 1. VertexService

The `VertexService` class is responsible for making HTTP requests to the Gemini API and processing the responses.

```kotlin
class VertexService {
    suspend fun getGeminiResponse(encodedImage: String, prompt: String): Result<Output>
}
```

#### Parameters:
- `encodedImage`: A Base64-encoded string representing the image.
- `prompt`: A string containing the text prompt for the LLM.

#### Return Value:
- `Result<Output>`: A Result object containing either an Output object with the LLM's response or an exception if the request failed.

### 2. GetGeminiRecipeUseCase

The `GetGeminiRecipeUseCase` class provides a higher-level interface for interacting with the Gemini LLM.

```kotlin
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) {
    suspend operator fun invoke(photo: ByteArray, availableProducts: String, isOfflineMode: Boolean = false): Result<Output>
}
```

#### Parameters:
- `photo`: A ByteArray containing the image data.
- `availableProducts`: A string containing a comma-separated list of available ingredients.
- `isOfflineMode`: A boolean flag indicating whether to use offline mode (for testing).

#### Return Value:
- `Result<Output>`: A Result object containing either an Output object with the LLM's response or an exception if the request failed.

## Request Structure

The request to the Gemini API is structured as follows:

### 1. HTTP Request

- **Method**: POST
- **URL**: `https://${API_ENDPOINT}/v1/projects/${PROJECT_ID}/locations/${LOCATION_ID}/publishers/google/models/${MODEL_ID}:streamGenerateContent`
- **Headers**:
  - `Content-Type`: application/json
  - `Authorization`: Bearer ${TOKEN}

### 2. Request Body

The request body is a JSON object with the following structure:

```json
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": "prompt_text"
        },
        {
          "inlineData": {
            "mimeType": "image/jpeg",
            "data": "base64_encoded_image"
          }
        }
      ]
    }
  ],
  "generationConfig": {
    "maxOutputTokens": 8192,
    "temperature": 1.0,
    "topP": 0.95
  },
  "safetySettings": [
    {
      "category": "HARM_CATEGORY_HATE_SPEECH",
      "threshold": "BLOCK_MEDIUM_AND_ABOVE"
    },
    {
      "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
      "threshold": "BLOCK_MEDIUM_AND_ABOVE"
    },
    {
      "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
      "threshold": "BLOCK_MEDIUM_AND_ABOVE"
    },
    {
      "category": "HARM_CATEGORY_HARASSMENT",
      "threshold": "BLOCK_MEDIUM_AND_ABOVE"
    }
  ]
}
```

#### Key Components:
- `contents`: A list of content objects, each with a role and a list of parts.
  - `role`: The role of the message sender (e.g., "user").
  - `parts`: A list of parts, which can be text or inline data (e.g., images).
- `generationConfig`: Configuration for text generation.
  - `maxOutputTokens`: The maximum number of tokens to generate (default: 8192).
  - `temperature`: The temperature for sampling (default: 1.0).
  - `topP`: The top-p value for nucleus sampling (default: 0.95).
- `safetySettings`: A list of safety settings for the LLM.
  - `category`: The harm category to set the threshold for.
  - `threshold`: The threshold level for the specified harm category.

### 3. Prompt Construction

The prompt is constructed using the following template:

```
Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients:
[available_products]
Then provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already.
format the response in a valid JSON object with all brackets matching in the following format
 Do not output markdown

output: {
    groceries: :string[],
    recipe {
        title: string,
        description: string,
        ingredients: string[],
        steps: string[]
    }
}
```

## Response Structure

The response from the Gemini API is structured as follows:

### 1. HTTP Response

- **Status Code**: 200-299 for success, other codes for failure.
- **Body**: A JSON array of CandidatesResponse objects.

### 2. Response Body

The response body is a JSON array with the following structure:

```json
[
  {
    "candidates": [
      {
        "content": {
          "role": "model",
          "parts": [
            {
              "text": "response_text"
            }
          ]
        }
      }
    ]
  }
]
```

#### Key Components:
- `candidates`: A list of candidate responses from the LLM.
  - `content`: The content of the response.
    - `role`: The role of the message sender (e.g., "model").
    - `parts`: A list of parts, which are typically text.

### 3. Output Format

The response text is parsed into an Output object with the following structure:

```kotlin
data class Output(
    val groceries: List<String> = emptyList(),
    val recipe: Recipe = Recipe()
)

data class Recipe(
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val title: String = ""
)
```

#### Key Components:
- `groceries`: A list of strings representing grocery items.
- `recipe`: A Recipe object containing:
  - `description`: A string describing the recipe.
  - `ingredients`: A list of strings representing ingredients.
  - `steps`: A list of strings representing the steps to prepare the recipe.
  - `title`: A string representing the title of the recipe.

## Authentication

The application uses a token-based authentication mechanism for accessing the Gemini API. The token is stored in the local.properties file as `VERTEX_TOKEN` and is included in the Authorization header of the HTTP request.

## Error Handling

The application uses Kotlin's Result type for error handling. If the request to the Gemini API fails, the Result will contain an exception with the error message. If the request succeeds but the response cannot be parsed, the Result will also contain an exception.

## Offline Mode

The application includes an offline mode for testing purposes. In offline mode, the application returns a mock response instead of making a request to the Gemini API.