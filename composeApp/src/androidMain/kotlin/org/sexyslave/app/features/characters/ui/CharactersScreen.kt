package org.sexyslave.app.features.characters.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import coil.compose.AsyncImage
import org.sexyslave.app.features.characters.mvi.CharactersViewModel
import kotlin.text.append

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*

import androidx.compose.ui.unit.dp

import androidx.paging.compose.collectAsLazyPagingItems
// import org.koin.androidx.compose.koinViewModel // Если используете koinViewModel()
import coil.compose.AsyncImage // Для загрузки изображений
import org.sexyslave.app.features.characters.data.api.Character


object CharactersScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: CharactersViewModel = getScreenModel() // <--- Используйте getScreenModel()
        val lazyCharacters = viewModel.charactersFlow.collectAsLazyPagingItems()

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Rick and Morty Characters") })
            }) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lazyCharacters.itemCount) { index ->
                    val character = lazyCharacters[index]
                    if (character != null) {
                        CharacterItem(
                            character = character, onClick = { /* TODO: Navigate to detail */ })
                    } else {
                        // Можно показать плейсхолдер во время загрузки элемента
                        CharacterItemPlaceholder()
                    }
                }

                // Обработка состояний загрузки Paging 3
                lazyCharacters.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item { FullScreenLoading() }
                        }

                        loadState.append is LoadState.Loading -> {
                            item { LoadingNextPageIndicator() }
                        }

                        loadState.refresh is LoadState.Error -> {
                            val e = lazyCharacters.loadState.refresh as LoadState.Error
                            item {
                                ErrorState(
                                    message = "Error loading characters: ${e.error.localizedMessage}",
                                    onRetry = { lazyCharacters.retry() })
                            }
                        }

                        loadState.append is LoadState.Error -> {
                            val e = lazyCharacters.loadState.append as LoadState.Error
                            item {
                                ErrorStateSmall(
                                    message = "Error loading more: ${e.error.localizedMessage}",
                                    onRetry = { lazyCharacters.retry() })
                            }
                        }
                        // Состояние, когда после первой загрузки список пуст
                        loadState.refresh is LoadState.NotLoading && lazyCharacters.itemCount == 0 -> {
                            item { EmptyState(message = "No characters found.") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterItem(character: Character, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Квадратное изображение
            )
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    4.dp
                )
            )
            Text(
                text = "Species: ${character.species}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    horizontal = 4.dp
                )
            )
            Text(
                text = "Status: ${character.status}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    horizontal = 4.dp
                )
            )
            Text(
                text = "Gender: ${character.gender}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    horizontal = 4.dp, vertical = 4.dp
                )
            )
        }
    }
}

@Composable
fun CharacterItemPlaceholder() { // Простой плейсхолдер
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
    ) { // Соотношение сторон как у CharacterItem
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
    }
}

@Composable
fun LoadingNextPageIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun ErrorStateSmall(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall)
    }
}