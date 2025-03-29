package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.dto.Ingredient
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GroceriesCard(modifier: Modifier = Modifier, groceries: List<Ingredient>) {
    LazyColumn(modifier = modifier) {
        items(groceries) { ingredient ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Quantity column (takes 30% of the width)
                Text(
                    text = ingredient.quantity,
                    modifier = Modifier.weight(0.3f)
                )
                // Name column (takes 70% of the width)
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
@Preview
fun GroceriesCardPreview() {
    val sampleGroceries = listOf(
        Ingredient(name = "flour", quantity = "250g"),
        Ingredient(name = "eggs", quantity = "2"),
        Ingredient(name = "milk", quantity = "1 cup"),
        Ingredient(name = "butter", quantity = "100g"),
        Ingredient(name = "salt", quantity = "1 tsp"),
        Ingredient(name = "sugar", quantity = "2 tbsp"),
        Ingredient(name = "chicken", quantity = "500g"),
        Ingredient(name = "tomatoes", quantity = "3"),
        Ingredient(name = "onion", quantity = "1"),
        Ingredient(name = "garlic", quantity = "2 cloves")
    )
    GroceriesCard(groceries = sampleGroceries)
}
