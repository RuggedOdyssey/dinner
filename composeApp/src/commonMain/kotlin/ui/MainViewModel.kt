package ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.Recipe
import data.llminference.LLMFactory
import data.preferences.PreferenceKeys
import data.preferences.PreferencesRepository
import domain.DietaryPreferences
import domain.GetGeminiRecipeUseCase
import domain.GetLocalRecipeUseCase
import domain.RecipeUseCase
import kotlinx.coroutines.Dispatchers
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

    // Pantry ingredient state
    private val _pantryIngredient = MutableStateFlow("")
    val pantryIngredient: StateFlow<String> = _pantryIngredient

    // Preferences repository for storing dietary preferences
    private val preferencesRepository = PreferencesRepository()

    // Dietary preferences state
    private val _dietaryPreferences = MutableStateFlow(DietaryPreferences())
    val dietaryPreferences: StateFlow<DietaryPreferences> = _dietaryPreferences

    // Show settings flag
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings

    private val getGeminiRecipe = GetGeminiRecipeUseCase()
    private val getLocalRecipe = GetLocalRecipeUseCase(createLLMFactory())

    init {
        // Update the DietaryPreferences state
        _dietaryPreferences.value = DietaryPreferences(preferencesRepository)
    }

    fun getRecipe(image: ByteArray, input: MutableState<String>, recipeTitle: MutableState<String>? = null) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading

        // Select the appropriate use case based on model type
        val useCase: RecipeUseCase = when (_modelType.value) {
            ModelType.ON_DEVICE -> getLocalRecipe
            ModelType.CLOUD -> getGeminiRecipe
        }

        // Invoke the selected use case and handle the result
        val result = useCase(image, input.value, recipeTitle?.value, _dietaryPreferences.value)
        if (result.isSuccess) {
            state.value = MainViewState.Success(result.getOrThrow())
        } else {
            result.exceptionOrNull()?.printStackTrace()
            state.value = MainViewState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
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
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.VEGETARIAN, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(vegetarian = value)
    }

    fun setLactoseFree(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.LACTOSE_FREE, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(lactoseFree = value)
    }

    fun setVegan(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.VEGAN, value)

        // If vegan is set to true, also set vegetarian, lactose free, no pork, and no seafood to true
        if (value) {
            preferencesRepository.saveBoolean(PreferenceKeys.VEGETARIAN, true)
            preferencesRepository.saveBoolean(PreferenceKeys.LACTOSE_FREE, true)
            preferencesRepository.saveBoolean(PreferenceKeys.NO_PORK, true)
            preferencesRepository.saveBoolean(PreferenceKeys.NO_SEAFOOD, true)

            // Update DietaryPreferences state with all related preferences
            _dietaryPreferences.value = _dietaryPreferences.value.copy(
                vegan = true,
                vegetarian = true,
                lactoseFree = true,
                noPork = true,
                noSeafood = true
            )
        } else {
            // Just update vegan preference
            _dietaryPreferences.value = _dietaryPreferences.value.copy(vegan = false)
        }
    }

    fun setGlutenFree(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.GLUTEN_FREE, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(glutenFree = value)
    }

    fun setNoSeafood(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.NO_SEAFOOD, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(noSeafood = value)
    }

    fun setNoPeanuts(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.NO_PEANUTS, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(noPeanuts = value)
    }

    fun setNoPork(value: Boolean) {
        // Update preference in repository
        preferencesRepository.saveBoolean(PreferenceKeys.NO_PORK, value)
        // Update DietaryPreferences state
        _dietaryPreferences.value = _dietaryPreferences.value.copy(noPork = value)
    }

    fun updatePantryIngredient(value: String) {
        _pantryIngredient.value = value
    }

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: Recipe) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}
