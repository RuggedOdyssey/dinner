package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.dto.Recipe

@Composable
fun RecipeCard(modifier: Modifier = Modifier, recipe:Recipe) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.h4
            )
        }
        item {
            Text(text = recipe.description)
        }
        items(recipe.ingredients) { item ->
            Text(text = item, modifier = Modifier.padding(8.dp))
        }
        item {
            Text(text = "Steps",
                style = MaterialTheme.typography.h4)
        }
        items(recipe.steps) { item ->
            Text(text = item, modifier = Modifier.padding(8.dp))
        }
    }
}