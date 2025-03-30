package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import data.dto.Recipe
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.CameraIcon
import ui.SettingsScreen

@Composable
@Preview
fun MainScreen() {
    val viewModel = remember { MainViewModel() }

    MaterialTheme {
        val viewState by viewModel.state.collectAsState()
        val modelType by viewModel.modelType.collectAsState()
        val showSettings by viewModel.showSettings.collectAsState()
        val showBack by remember { derivedStateOf { viewState !is MainViewModel.MainViewState.Input || showSettings } }
        val screenTitle = "What's for dinner?"

        // Use WindowInsets.safeDrawing for edge-to-edge display
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = { 
                                if (showSettings) {
                                    viewModel.toggleSettings()
                                } else {
                                    viewModel.back()
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        // Hide settings icon when recipe card and groceries are shown
                        if (viewState !is MainViewModel.MainViewState.Success) {
                            IconButton(onClick = viewModel::toggleSettings) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    },
                    // Apply top insets to the TopAppBar and make it taller
                    modifier = Modifier
                        .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
                )
            },
            // Set contentPadding to handle system bars
            contentColor = MaterialTheme.colors.onBackground

        ) { innerPadding ->
            if (showSettings) {
                SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                when (viewState) {
                    is MainViewModel.MainViewState.Input -> {
                        val pantryIngredient by viewModel.pantryIngredient.collectAsState()
                        InputScreen(
                            modifier = Modifier.padding(innerPadding),
                            getRecipe = { image, pantryIngredient, recipeTitle -> 
                                viewModel.getRecipe(image, pantryIngredient, recipeTitle)
                            },
                            modelType = modelType,
                            setModelType = viewModel::setModelType,
                            pantryIngredient = pantryIngredient,
                            updatePantryIngredient = viewModel::updatePantryIngredient
                        )
                    }

                    is MainViewModel.MainViewState.Loading -> {
                        ProgressScreen(
                            modifier = Modifier.padding(innerPadding),
                        )
                    }

                    is MainViewModel.MainViewState.Success -> {
                        val recipe = (viewState as MainViewModel.MainViewState.Success).result
                        RecipeScreen(
                            modifier = Modifier.padding(innerPadding), recipe = recipe
                        )
                    }

                    is MainViewModel.MainViewState.Error -> {
                        ErrorScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewState = viewState as MainViewModel.MainViewState.Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputScreen(
    modifier: Modifier = Modifier,
    getRecipe: (ByteArray, MutableState<String>, MutableState<String>?) -> Unit,
    modelType: ModelType,
    setModelType: (ModelType) -> Unit,
    pantryIngredient: String,
    updatePantryIngredient: (String) -> Unit
) {
    val pantryIngredientState = remember { mutableStateOf(pantryIngredient) }
    val recipeTitle = remember { mutableStateOf("") }
    val state = rememberPeekabooCameraState(onCapture = {
        it?.let { image ->
            getRecipe(image, pantryIngredientState, if (modelType == ModelType.ON_DEVICE) recipeTitle else null)
        }
    })

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Two-way tab for model selection
            val modelTypes = listOf(ModelType.ON_DEVICE, ModelType.CLOUD)
            val modelTypeLabels = listOf("On-device", "Cloud")
            val selectedTabIndex = modelTypes.indexOf(modelType)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                modelTypes.forEachIndexed { index, type ->
                    Tab(
                        selected = modelType == type,
                        onClick = { setModelType(type) },
                        text = { Text(text = modelTypeLabels[index]) }
                    )
                }
            }

            TextField(
                value = pantryIngredientState.value,
                onValueChange = { 
                    pantryIngredientState.value = it
                    updatePantryIngredient(it)
                },
                label = { Text("Enter available products") },
                modifier = Modifier.padding(top = 16.dp)
            )

            // Only show recipe title field when on-device model is selected
            if (modelType == ModelType.ON_DEVICE) {
                TextField(
                    value = recipeTitle.value,
                    onValueChange = { recipeTitle.value = it },
                    label = { Text("Enter recipe title") },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Only show camera preview when cloud model is selected
            if (modelType == ModelType.CLOUD) {
                PeekabooCamera(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    permissionDeniedContent = {
                        Text("Permission denied")
                    },
                )
            }

            // Submit button for on-device mode
            if (modelType == ModelType.ON_DEVICE) {
                Button(
                    onClick = { 
                        // Call getRecipe with empty photo data
                        getRecipe(ByteArray(0), pantryIngredientState, if (modelType == ModelType.ON_DEVICE) recipeTitle else null)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Submit")
                }
            }
        }

        // Floating Action Button at the bottom center - only show in cloud mode
        if (modelType == ModelType.CLOUD) {
            FloatingActionButton(
                onClick = { state.capture() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 16.dp
                    )
            ) {
                CameraIcon()
            }
        }

    }
}

@Composable
private fun ProgressScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
private fun RecipeScreen(modifier: Modifier = Modifier, recipe: Recipe) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Recipe", "Groceries")
    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> RecipeCard(recipe = recipe)
            1 -> GroceriesCard(groceries = recipe.ingredients)
        }
    }
}

@Composable
private fun ErrorScreen(
    modifier: Modifier = Modifier,
    viewState: MainViewModel.MainViewState.Error
) {
    Text(modifier = modifier, text = viewState.text)
}
