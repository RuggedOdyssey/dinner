package ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.Output
import data.llminference.LLMFactory
import data.preferences.PreferenceKeys
import data.preferences.PreferencesRepository
import domain.DietaryPreferences
import domain.GetGeminiRecipeUseCase
import domain.GetLocalRecipeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ModelType {
    ON_DEVICE, // On-device model
    CLOUD // Online mode with cloud model
}

expect fun createLLMFactory(): LLMFactory

class MainViewModel : ViewModel() {

    val state = MutableStateFlow<MainViewState>(MainViewState.Input)
    private val _modelType = MutableStateFlow(ModelType.ON_DEVICE) // Default to on-device mode
    val modelType: StateFlow<ModelType> = _modelType

    // Preferences repository for storing dietary preferences
    private val preferencesRepository = PreferencesRepository()

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
    private val getLocalRecipe = GetLocalRecipeUseCase(createLLMFactory())

    init {
        // Load dietary preferences from storage
        _vegetarian.value = preferencesRepository.getBoolean(PreferenceKeys.VEGETARIAN)
        _lactoseFree.value = preferencesRepository.getBoolean(PreferenceKeys.LACTOSE_FREE)
        _vegan.value = preferencesRepository.getBoolean(PreferenceKeys.VEGAN)
        _glutenFree.value = preferencesRepository.getBoolean(PreferenceKeys.GLUTEN_FREE)
        _noSeafood.value = preferencesRepository.getBoolean(PreferenceKeys.NO_SEAFOOD)
        _noPeanuts.value = preferencesRepository.getBoolean(PreferenceKeys.NO_PEANUTS)
        _noPork.value = preferencesRepository.getBoolean(PreferenceKeys.NO_PORK)
    }

    fun getRecipe(image: ByteArray, input: MutableState<String>, recipeTitle: MutableState<String>? = null) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading

        // Create dietary preferences object
        val dietaryPreferences = createDietaryPreferences()

        when (_modelType.value) {
            ModelType.ON_DEVICE -> {
                // Use on-device LLM model
                val result = getLocalRecipe(image, input.value, recipeTitle?.value, dietaryPreferences)
                if (result.isSuccess) {
                    state.value = MainViewState.Success(result.getOrThrow())
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                    state.value = MainViewState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
                }
            }
            ModelType.CLOUD -> {
                // Use real Gemini service in online mode
                val result = getGeminiRecipe(image, input.value, recipeTitle?.value, dietaryPreferences)
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
        preferencesRepository.saveBoolean(PreferenceKeys.VEGETARIAN, value)
    }

    fun setLactoseFree(value: Boolean) {
        _lactoseFree.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.LACTOSE_FREE, value)
    }

    fun setVegan(value: Boolean) {
        _vegan.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.VEGAN, value)
    }

    fun setGlutenFree(value: Boolean) {
        _glutenFree.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.GLUTEN_FREE, value)
    }

    fun setNoSeafood(value: Boolean) {
        _noSeafood.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.NO_SEAFOOD, value)
    }

    fun setNoPeanuts(value: Boolean) {
        _noPeanuts.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.NO_PEANUTS, value)
    }

    fun setNoPork(value: Boolean) {
        _noPork.value = value
        preferencesRepository.saveBoolean(PreferenceKeys.NO_PORK, value)
    }

    /**
     * Creates a DietaryPreferences object from the current preference values.
     */
    private fun createDietaryPreferences(): DietaryPreferences {
        return DietaryPreferences(
            vegetarian = _vegetarian.value,
            lactoseFree = _lactoseFree.value,
            vegan = _vegan.value,
            glutenFree = _glutenFree.value,
            noSeafood = _noSeafood.value,
            noPeanuts = _noPeanuts.value,
            noPork = _noPork.value
        )
    }

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: Output) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}
