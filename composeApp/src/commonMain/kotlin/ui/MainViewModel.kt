package ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.Output
import domain.GetGeminiRecipeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val state = MutableStateFlow<MainViewState>(MainViewState.Input)
    private val getGeminiRecipe = GetGeminiRecipeUseCase()

    fun getRecipe(image: ByteArray, input: MutableState<String>) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading

        val result = getGeminiRecipe(image, input.value)
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

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: Output) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}