package ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.Output
import data.dto.Recipe
import data.llminference.LLMFactory
import domain.GetGeminiRecipeUseCase
import domain.GetLocalRecipeUseCase
import domain.GetMockGeminiRecipeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ModelType {
    MOCK, // Offline mode with mock data
    ON_DEVICE, // On-device model
    CLOUD // Online mode with cloud model
}

enum class DietaryPreference {
    VEGETARIAN,
    LACTOSE_FREE,
    VEGAN,
    GLUTEN_FREE,
    NO_SEAFOOD,
    NO_PEANUTS,
    NO_PORK
}

expect fun createLLMFactory(): LLMFactory

class MainViewModel : ViewModel() {

    val state = MutableStateFlow<MainViewState>(MainViewState.Input)
    private val _modelType = MutableStateFlow(ModelType.MOCK) // Default to mock mode
    val modelType: StateFlow<ModelType> = _modelType

    // Dietary preference flags
    private val _vegetarian = MutableStateFlow(false)
    val vegetarian: StateFlow<Boolean> = _vegetarian

    private val _lactoseFree = MutableStateFlow(false)
    val lactoseFree: StateFlow<Boolean> = _lactoseFree

    private val _vegan = MutableStateFlow(false)
    val vegan: StateFlow<Boolean> = _vegan

    private val _glutenFree = MutableStateFlow(false)
    val glutenFree: StateFlow<Boolean> = _glutenFree

    private val _noSeafood = MutableStateFlow(false)
    val noSeafood: StateFlow<Boolean> = _noSeafood

    private val _noPeanuts = MutableStateFlow(false)
    val noPeanuts: StateFlow<Boolean> = _noPeanuts

    private val _noPork = MutableStateFlow(false)
    val noPork: StateFlow<Boolean> = _noPork

    // Show settings flag
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings

    private val getGeminiRecipe = GetGeminiRecipeUseCase()
    private val getMockGeminiRecipe = GetMockGeminiRecipeUseCase()
    private val getLocalRecipe = GetLocalRecipeUseCase(createLLMFactory())

    fun getRecipe(image: ByteArray, input: MutableState<String>, recipeTitle: MutableState<String>? = null) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading

        when (_modelType.value) {
            ModelType.ON_DEVICE -> {
                // Use on-device LLM model
                val ingredients = input.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                getLocalRecipe.getRecipe(image, ingredients).collectLatest { response ->
                    if (response == "Loading..." || response.contains("Downloading model")) {
                        state.value = MainViewState.Loading
                    } else {
                        // Parse the LLM output into our data structure
                        // This is a simplified parser - in a real app you'd want more robust parsing
                        val lines = response.split("\n")
                        val title = recipeTitle?.value?.takeIf { it.isNotBlank() } ?: lines.firstOrNull() ?: "Recipe"

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
            }
            ModelType.MOCK -> {
                // Use mock Gemini service
                val result = getMockGeminiRecipe(image, input.value, recipeTitle?.value)
                if (result.isSuccess) {
                    state.value = MainViewState.Success(result.getOrThrow())
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                    state.value = MainViewState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
                }
            }
            ModelType.CLOUD -> {
                // Use real Gemini service in online mode
                val result = getGeminiRecipe(image, input.value, recipeTitle?.value)
                if (result.isSuccess) {
                    state.value = MainViewState.Success(result.getOrThrow())
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                    state.value = MainViewState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
                }
            }
        }
    }

    fun back() {
        state.value = MainViewState.Input
    }

    fun setModelType(type: ModelType) {
        _modelType.value = type
    }

    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun setVegetarian(value: Boolean) {
        _vegetarian.value = value
    }

    fun setLactoseFree(value: Boolean) {
        _lactoseFree.value = value
    }

    fun setVegan(value: Boolean) {
        _vegan.value = value
    }

    fun setGlutenFree(value: Boolean) {
        _glutenFree.value = value
    }

    fun setNoSeafood(value: Boolean) {
        _noSeafood.value = value
    }

    fun setNoPeanuts(value: Boolean) {
        _noPeanuts.value = value
    }

    fun setNoPork(value: Boolean) {
        _noPork.value = value
    }

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: Output) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}
