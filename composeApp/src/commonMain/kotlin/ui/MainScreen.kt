package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MainScreen() {

    val viewModel = remember { MainViewModel() }

    MaterialTheme {
        val viewState by viewModel.state.collectAsState()
        when (viewState) {
            is MainViewModel.MainViewState.Input -> {
                val input = remember { mutableStateOf("") }
                val state = rememberPeekabooCameraState(onCapture = {
                    it?.let { image ->
                        viewModel.getRecipe(image, input)
                    }
                })
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(
                        value = input.value,
                        onValueChange = { input.value = it },
                        label = { Text("Enter available products") }
                    )
                    Button(onClick = {
                        state.capture()
                    }) {
                        Text("Take photo")
                    }
                    PeekabooCamera(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        permissionDeniedContent = {
                            Text("Please grant camera permission")
                        },
                    )
                }
            }


            is MainViewModel.MainViewState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

            }

            is MainViewModel.MainViewState.Success -> {
                val output = (viewState as MainViewModel.MainViewState.Success).result
                var selectedTabIndex by remember { mutableStateOf(0) }
                val tabs = listOf("Groceries", "Recipe")
                Column {
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

            is MainViewModel.MainViewState.Error -> {
                Text((viewState as MainViewModel.MainViewState.Error).text)
            }

        }
    }
}