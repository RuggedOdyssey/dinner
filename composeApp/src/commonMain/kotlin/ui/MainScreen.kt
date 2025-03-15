package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import data.dto.Output
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MainScreen() {
    val viewModel = remember { MainViewModel() }

    MaterialTheme {
        val viewState by viewModel.state.collectAsState()
        val isOfflineMode by viewModel.isOfflineMode.collectAsState()
        val useOnDeviceModel by viewModel.useOnDeviceModel.collectAsState()
        val showBack by remember { derivedStateOf { viewState !is MainViewModel.MainViewState.Input } }
        val screenTitle by remember { derivedStateOf { if (!showBack) "What's for dinner?" else "" } }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = viewModel::back) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }

        ) { innerPadding ->
            when (viewState) {
                is MainViewModel.MainViewState.Input -> {
                    InputScreen(
                        modifier = Modifier.padding(innerPadding),
                        getRecipe = viewModel::getRecipe,
                        isOfflineMode = isOfflineMode,
                        toggleOfflineMode = viewModel::toggleOfflineMode,
                        useOnDeviceModel = useOnDeviceModel,
                        toggleModelType = viewModel::toggleModelType
                    )
                }

                is MainViewModel.MainViewState.Loading -> {
                    ProgressScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                is MainViewModel.MainViewState.Success -> {
                    val output = (viewState as MainViewModel.MainViewState.Success).result
                    RecipeScreen(
                        modifier = Modifier.padding(innerPadding), output = output
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

@Composable
private fun InputScreen(
    modifier: Modifier = Modifier,
    getRecipe: (ByteArray, MutableState<String>) -> Unit,
    isOfflineMode: Boolean,
    toggleOfflineMode: () -> Unit,
    useOnDeviceModel: Boolean,
    toggleModelType: () -> Unit
) {
    val input = remember { mutableStateOf("") }
    val state = rememberPeekabooCameraState(onCapture = {
        it?.let { image ->
            getRecipe(image, input)
        }
    })
    
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = input.value,
            onValueChange = { input.value = it },
            label = { Text("Enter available products") }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = {
                state.capture()
            }) {
                Text("Take photo")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = isOfflineMode,
                onCheckedChange = { toggleOfflineMode() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = if (isOfflineMode) "Offline" else "Online")
        }
        
        // Add a row for on-device model toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(text = "Model: ")
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Switch(
                checked = useOnDeviceModel,
                onCheckedChange = { toggleModelType() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(text = if (useOnDeviceModel) "On-device" else "Cloud")
        }

        PeekabooCamera(
            state = state,
            modifier = Modifier.fillMaxSize(),
            permissionDeniedContent = {
                Text("Permission denied")
            },
        )
    }
}

@Composable
private fun ProgressScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
private fun RecipeScreen(modifier: Modifier = Modifier, output: Output) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Groceries", "Recipe")
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
            0 -> GroceriesCard(groceries = output.groceries)
            1 -> RecipeCard(recipe = output.recipe)
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
