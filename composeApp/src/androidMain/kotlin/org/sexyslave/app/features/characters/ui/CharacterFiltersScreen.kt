package org.sexyslave.app.features.characters.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.sexyslave.app.features.characters.data.CharacterFilters
import org.sexyslave.app.features.characters.data.CharacterGender
import org.sexyslave.app.features.characters.data.CharacterStatus

data class CharacterFiltersScreen(
    val initialFilters: CharacterFilters,
    val onApplyFilters: (CharacterFilters) -> Unit,
    val onClearFilters: () -> Unit // Добавим явный колбэк для очистки из CharactersViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Локальное состояние для полей ввода, инициализированное initialFilters
        var nameState by remember { mutableStateOf(initialFilters.name ?: "") }
        var statusState by remember { mutableStateOf(initialFilters.status) }
        var speciesState by remember { mutableStateOf(initialFilters.species ?: "") }
        var typeState by remember { mutableStateOf(initialFilters.type ?: "") }
        var genderState by remember { mutableStateOf(initialFilters.gender) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Filter Characters") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // Сбрасываем локальное состояние UI и вызываем колбэк
                            nameState = ""
                            statusState = null
                            speciesState = ""
                            typeState = ""
                            genderState = null
                            onClearFilters() // Вызываем колбэк, который обновит ViewModel

                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Filters")
                        }
                    }
                )
            },
            bottomBar = {
                Button(
                    onClick = {
                        val appliedFilters = CharacterFilters(
                            name = nameState.takeIf { it.isNotBlank() },
                            status = statusState,
                            species = speciesState.takeIf { it.isNotBlank() },
                            type = typeState.takeIf { it.isNotBlank() },
                            gender = genderState
                        )
                        onApplyFilters(appliedFilters)
                        navigator.pop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FilterDropdownMenu(
                    label = "Status",
                    selectedValue = statusState?.apiValue ?: "Any",
                    items = listOf("Any") + CharacterStatus.entries.map { it.apiValue },
                    onItemSelected = { selectedApiValue ->
                        statusState = if (selectedApiValue == "Any") null else CharacterStatus.fromApiValue(selectedApiValue)
                    }
                )

                OutlinedTextField(
                    value = speciesState,
                    onValueChange = { speciesState = it },
                    label = { Text("Species") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = typeState,
                    onValueChange = { typeState = it },
                    label = { Text("Type (e.g., Parasite, Human with ants in his eyes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FilterDropdownMenu(
                    label = "Gender",
                    selectedValue = genderState?.apiValue ?: "Any",
                    items = listOf("Any") + CharacterGender.entries.map { it.apiValue },
                    onItemSelected = { selectedApiValue ->
                        genderState = if (selectedApiValue == "Any") null else CharacterGender.fromApiValue(selectedApiValue)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdownMenu(
    label: String,
    selectedValue: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
