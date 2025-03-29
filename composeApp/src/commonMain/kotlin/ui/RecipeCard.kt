package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.dto.Recipe
import data.dto.Ingredient
import org.jetbrains.compose.ui.tooling.preview.Preview
import util.rememberPlatformClipboardUtil

@Composable
fun RecipeCard(modifier: Modifier = Modifier, recipe:Recipe) {
    val clipboardUtil = rememberPlatformClipboardUtil()

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { clipboardUtil.copyToClipboard(recipe.title) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Copy")
                }
            }
        }
        item {
            Text(
                text = recipe.description,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(recipe.ingredients) { ingredient ->
            Text(
                text = "${ingredient.quantity} ${ingredient.name}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        item {
            Text(
                text = "Steps",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        items(recipe.steps) { item ->
            Text(text = item, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
@Preview
fun RecipeCardPreview() {
    val sampleRecipe = Recipe(
        title = "Pancakes",
        description = "Fluffy and delicious pancakes that are perfect for breakfast or brunch.",
        ingredients = listOf(
            Ingredient(name = "flour", quantity = "250g"),
            Ingredient(name = "eggs", quantity = "2"),
            Ingredient(name = "milk", quantity = "1 cup"),
            Ingredient(name = "sugar", quantity = "2 tbsp"),
            Ingredient(name = "baking powder", quantity = "1 tsp"),
            Ingredient(name = "salt", quantity = "1/2 tsp"),
            Ingredient(name = "melted butter", quantity = "2 tbsp")
        ),
        steps = listOf(
            "1. In a large bowl, mix flour, sugar, baking powder, and salt.",
            "2. In another bowl, beat the eggs, then add milk and melted butter.",
            "3. Pour the wet ingredients into the dry ingredients and stir until just combined.",
            "4. Heat a lightly oiled frying pan over medium-high heat.",
            "5. Pour 1/4 cup of batter onto the pan for each pancake.",
            "6. Cook until bubbles form on the surface, then flip and cook until golden brown.",
            "7. Serve hot with maple syrup, fresh fruits, or whipped cream."
        )
    )
    RecipeCard(recipe = sampleRecipe)
}
