package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vegetarian by viewModel.vegetarian.collectAsState()
    val lactoseFree by viewModel.lactoseFree.collectAsState()
    val vegan by viewModel.vegan.collectAsState()
    val glutenFree by viewModel.glutenFree.collectAsState()
    val noSeafood by viewModel.noSeafood.collectAsState()
    val noPeanuts by viewModel.noPeanuts.collectAsState()
    val noPork by viewModel.noPork.collectAsState()

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Dietary Preferences",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DietaryPreferenceItem(
                text = "Vegetarian",
                checked = vegetarian,
                onCheckedChange = { viewModel.setVegetarian(it) }
            )

            DietaryPreferenceItem(
                text = "Lactose Free",
                checked = lactoseFree,
                onCheckedChange = { viewModel.setLactoseFree(it) }
            )

            DietaryPreferenceItem(
                text = "Vegan",
                checked = vegan,
                onCheckedChange = { viewModel.setVegan(it) }
            )

            DietaryPreferenceItem(
                text = "Gluten Free",
                checked = glutenFree,
                onCheckedChange = { viewModel.setGlutenFree(it) }
            )

            DietaryPreferenceItem(
                text = "No Seafood",
                checked = noSeafood,
                onCheckedChange = { viewModel.setNoSeafood(it) }
            )

            DietaryPreferenceItem(
                text = "No Peanuts",
                checked = noPeanuts,
                onCheckedChange = { viewModel.setNoPeanuts(it) }
            )

            DietaryPreferenceItem(
                text = "No Pork",
                checked = noPork,
                onCheckedChange = { viewModel.setNoPork(it) }
            )
        }
    }
}

@Composable
private fun DietaryPreferenceItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = checked,
            onClick = { onCheckedChange(!checked) }
        )
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}