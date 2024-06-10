import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

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
                            // Custom UI content for permission denied scenario
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
                // TODO fix scroll
                Box(Modifier.fillMaxSize().scrollable(rememberScrollState(), orientation = Orientation.Vertical)) {
                    Text((viewState as MainViewModel.MainViewState.Success).result)
                }
            }

            is MainViewModel.MainViewState.Error -> {
                Text((viewState as MainViewModel.MainViewState.Error).text)
            }
        }
    }
}