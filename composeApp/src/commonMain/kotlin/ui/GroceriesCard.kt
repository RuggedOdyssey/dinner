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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GroceriesCard(modifier: Modifier = Modifier, groceries: List<String>) {
    LazyColumn(modifier = modifier) {
        items(groceries) { item ->
            // Split the item into amount and name
            // Assuming format is like "250g flour", "2 eggs", etc.
            val parts = item.split(" ", limit = 2)
            val amount = if (parts.size > 1) parts[0] else ""
            val itemName = if (parts.size > 1) parts[1] else item

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Amount column (takes 30% of the width)
                Text(
                    text = amount,
                    modifier = Modifier.weight(0.3f)
                )
                // Item name column (takes 70% of the width)
                Text(
                    text = itemName,
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
        "250g flour",
        "2 eggs",
        "1 cup milk",
        "100g butter",
        "1 tsp salt",
        "2 tbsp sugar",
        "500g chicken",
        "3 tomatoes",
        "1 onion",
        "2 cloves garlic"
    )
    GroceriesCard(groceries = sampleGroceries)
}
