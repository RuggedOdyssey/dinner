package net.ruggedodyssey.whatsfordinner

import ui.MainScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Store the instance for access by other components
        instance = this

        setContent {
            MainScreen()
        }
    }
    
    companion object {
        private lateinit var instance: MainActivity
        
        fun getInstance(): MainActivity {
            return instance
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MainScreen()
}