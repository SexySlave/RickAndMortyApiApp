package org.sexyslave.app.features.characters.ui

// ... другие импорты ...
import androidx.compose.material.ExperimentalMaterialApi // Убедитесь, что это есть
import androidx.compose.material3.ExperimentalMaterial3Api // Убедитесь, что это есть
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue // Если используется для isRefreshing, хотя в вашем коде напрямую
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel // <--- Вот этот импорт важен
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import org.sexyslave.app.features.characters.data.api.Character // Предполагается, что Character здесь определен
import org.sexyslave.app.features.characters.mvi.CharactersViewModel


object CharactersScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow // Получаем навигатор Voyager
        val viewModel: CharactersViewModel = getScreenModel() // Используем getScreenModel
        val lazyCharacters = viewModel.charactersFlow.collectAsLazyPagingItems()

        val isRefreshing = lazyCharacters.loadState.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { lazyCharacters.refresh() }
        )

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Rick and Morty Characters") })
            }) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lazyCharacters.loadState.refresh !is LoadState.Loading || lazyCharacters.itemCount > 0) {
                        items(lazyCharacters.itemCount, key = { index -> lazyCharacters.peek(index)?.id ?: -1}) { index ->
                            val character = lazyCharacters[index]
                            if (character != null) {
                                CharacterItem(
                                    character = character,
                                    onClick = {
                                        // Вот здесь навигация:
                                        navigator.push(CharacterDetailScreen(character.id))
                                    }
                                )
                            } else {
                                CharacterItemPlaceholder()
                            }
                        }
                    }
                    // ... остальная часть кода для состояний загрузки ...
                     lazyCharacters.apply {
                        when {
                            loadState.append is LoadState.Loading -> {
                                item { LoadingNextPageIndicator() }
                            }
                            loadState.refresh is LoadState.Error -> {
                                val e = loadState.refresh as LoadState.Error
                                item {
                                    ErrorState(
                                        message = "Error loading characters: ${e.error.localizedMessage}",
                                        onRetry = { lazyCharacters.retry() }
                                    )
                                }
                            }
                            loadState.append is LoadState.Error -> {
                                val e = loadState.append as LoadState.Error
                                item {
                                    ErrorStateSmall(
                                        message = "Error loading more: ${e.error.localizedMessage}",
                                        onRetry = { lazyCharacters.retry() }
                                    )
                                }
                            }
                            loadState.refresh is LoadState.NotLoading && lazyCharacters.itemCount == 0 && !isRefreshing -> {
                                item {
                                     EmptyState(message = "No characters found.")
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

// CharacterItem и другие Composable функции (Placeholder, Loading, Error, Empty) остаются как есть,
// так как они были определены в вашем исходном файле.
// Убедитесь, что CharacterItem принимает character: Character и onClick: () -> Unit.
// Я скопирую их из вашего первоначального контекста для полноты:

@Composable
fun CharacterItem(character: Character, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "Species: ${character.species}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = "Status: ${character.status}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = if (character.gender.isBlank()) 4.dp else 0.dp)
            )
            if (character.gender.isNotBlank()) {
                Text(
                    text = "Gender: ${character.gender}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CharacterItemPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.75f)) { // Соотношение сторон как у CharacterItem
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
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
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
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
        Text(text = message, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
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
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall)
    }
}
