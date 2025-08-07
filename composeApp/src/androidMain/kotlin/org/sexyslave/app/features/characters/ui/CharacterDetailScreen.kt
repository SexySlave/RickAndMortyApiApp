package org.sexyslave.app.features.characters.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.mvi.CharacterDetailUiState
import org.sexyslave.app.features.characters.mvi.CharacterDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class CharacterDetailScreen(val characterId: Int) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Используем koinViewModel с параметрами для передачи characterId
        val viewModel: CharacterDetailViewModel = koinViewModel { parametersOf(characterId) }
        val uiState by viewModel.uiState.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Character Details") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when (val state = uiState) {
                    is CharacterDetailUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is CharacterDetailUiState.Success -> {
                        CharacterDetailsContent(character = state.character)
                    }
                    is CharacterDetailUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.fetchCharacterDetails() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CharacterDetailsContent(character: Character) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AsyncImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )
            Text(character.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))
        }

        item { DetailItem("ID:", character.id.toString()) }
        item { DetailItem("Status:", character.status) }
        item { DetailItem("Species:", character.species) }
        if (character.type.isNotBlank()) {
            item { DetailItem("Type:", character.type) }
        }
        item { DetailItem("Gender:", character.gender) }
        item { DetailItem("Origin:", character.origin.name) }
        item { DetailItem("Last Known Location:", character.location.name) }
        item { DetailItem("Appeared in episodes:", character.episode.size.toString()) }

        // Форматирование даты
        val formattedCreatedDate = try {
            val odt = OffsetDateTime.parse(character.created)
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault())
            odt.format(formatter)
        } catch (e: Exception) {
            character.created // Если не удалось распарсить, показать как есть
        }
        item { DetailItem("Created:", formattedCreatedDate) }
        item { DetailItem("API URL:", character.url, isUrl = true) }

        item { Spacer(modifier = Modifier.height(16.dp)) } // Отступ в конце
    }
}

@Composable
fun DetailItem(label: String, value: String, isUrl: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.4f) // Метка занимает 40%
        )
        if (isUrl) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary, // Можно сделать кликабельной
                modifier = Modifier.weight(0.6f)
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
}
