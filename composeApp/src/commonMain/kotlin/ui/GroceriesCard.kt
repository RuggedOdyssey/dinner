package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroceriesCard(modifier: Modifier = Modifier, groceries: List<String>) {
    LazyColumn(modifier = modifier) {
        items(groceries) { item ->
            Text(text = item, modifier = Modifier.padding(16.dp))
        }
    }
}
