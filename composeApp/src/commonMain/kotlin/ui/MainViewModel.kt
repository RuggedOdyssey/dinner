package ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.Output
import data.dto.Recipe
import data.mediapipe.LLMFactory
import domain.GetGeminiRecipeUseCase
import domain.GetLocalRecipeUseCase
import domain.GetMockGeminiRecipeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

expect fun createLLMFactory(): LLMFactory

class MainViewModel : ViewModel() {

    val state = MutableStateFlow<MainViewState>(MainViewState.Input)
    private val _isOfflineMode = MutableStateFlow(true) // Default to offline mode
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode
    private val _useOnDeviceModel = MutableStateFlow(false) // Default to cloud model even in offline mode
    val useOnDeviceModel: StateFlow<Boolean> = _useOnDeviceModel

    private val getGeminiRecipe = GetGeminiRecipeUseCase()
    private val getMockGeminiRecipe = GetMockGeminiRecipeUseCase()
    private val getLocalRecipe = GetLocalRecipeUseCase(createLLMFactory())

    fun getRecipe(image: ByteArray, input: MutableState<String>) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading

        if (_useOnDeviceModel.value) {
            // Use on-device LLM model
            val ingredients = input.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            getLocalRecipe.getRecipe(ingredients).collectLatest { response ->
                if (response == "Loading..." || response.contains("Downloading model")) {
                    state.value = MainViewState.Loading
                } else {
                    // Parse the LLM output into our data structure
                    // This is a simplified parser - in a real app you'd want more robust parsing
                    val lines = response.split("\n")
                    val title = lines.firstOrNull() ?: "Recipe"

                    val ingredientsList = mutableListOf<String>()
                    val steps = mutableListOf<String>()

                    var inIngredients = false
                    var inSteps = false

                    lines.forEach { line ->
                        when {
                            line.contains("Ingredients", ignoreCase = true) && !inIngredients && !inSteps -> {
                                inIngredients = true
                            }
                            line.contains("Instructions", ignoreCase = true) || 
                                 line.contains("Directions", ignoreCase = true) || 
                                 line.contains("Steps", ignoreCase = true) -> {
                                inIngredients = false
                                inSteps = true
                            }
                            inIngredients && line.isNotBlank() && !line.contains("Ingredients", ignoreCase = true) -> {
                                ingredientsList.add(line.trim())
                            }
                            inSteps && line.isNotBlank() && 
                                !line.contains("Instructions", ignoreCase = true) && 
                                !line.contains("Directions", ignoreCase = true) && 
                                !line.contains("Steps", ignoreCase = true) -> {
                                steps.add(line.trim())
                            }
                        }
                    }

                    val recipe = Recipe(
                        title = title,
                        description = "Generated with on-device LLM",
                        ingredients = ingredientsList,
                        steps = steps
                    )

                    val output = Output(
                        groceries = ingredients,
                        recipe = recipe
                    )

                    state.value = MainViewState.Success(output)
                }
            }
        } else {
            // Check if we're in offline mode
            val result = if (_isOfflineMode.value) {
                // Use mock Gemini service in offline mode
                getMockGeminiRecipe(image, input.value)
            } else {
                // Use real Gemini service in online mode
                getGeminiRecipe(image, input.value)
            }

            if (result.isSuccess) {
                state.value = MainViewState.Success(result.getOrThrow())
            } else {
                result.exceptionOrNull()?.printStackTrace()
                state.value = MainViewState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }

    fun back() {
        state.value = MainViewState.Input
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun toggleModelType() {
        _useOnDeviceModel.value = !_useOnDeviceModel.value
    }

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: Output) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}
