import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.preat.peekaboo.image.picker.toImageBitmap
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    val viewModel =  remember { MainViewModel() }

    MaterialTheme {
        val state = rememberPeekabooCameraState(onCapture = {
            val image = it?.toImageBitmap()
            viewModel.uploadImage(image)
        })
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
}