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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.pullrefresh.PullRefreshIndicator // <--- Импорт для индикатора
import androidx.compose.material.pullrefresh.pullRefresh // <--- Импорт для модификатора
import androidx.compose.material.pullrefresh.rememberPullRefreshState // <--- Импорт для состояния
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // <--- Для remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import coil.compose.AsyncImage
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.mvi.CharactersViewModel


object CharactersScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class) // Уже было
    @Composable
    override fun Content() {
        val viewModel: CharactersViewModel = getScreenModel()
        val lazyCharacters = viewModel.charactersFlow.collectAsLazyPagingItems()

        // Состояние для Pull-to-Refresh
        // Мы считаем, что идет обновление, если Paging находится в состоянии refresh LoadState.Loading
        val isRefreshing = lazyCharacters.loadState.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { lazyCharacters.refresh() } // Вызываем refresh() у PagingDataAdapter
        )

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Rick and Morty Characters") })
            }) { paddingValues ->
            Box( // Оборачиваем LazyVerticalGrid в Box для позиционирования PullRefreshIndicator
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState) // Применяем модификатор pullRefresh
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(), // LazyGrid теперь заполняет Box
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Не показываем FullScreenLoading здесь, так как PullRefreshIndicator его заменит
                    // когда isRefreshing = true и itemCount == 0 (первая загрузка)
                    // или когда isRefreshing = true и itemCount > 0 (обновление)

                    if (lazyCharacters.loadState.refresh !is LoadState.Loading || lazyCharacters.itemCount > 0) {
                        items(lazyCharacters.itemCount, key = { index -> lazyCharacters.peek(index)?.id ?: -1}) { index ->
                            val character = lazyCharacters[index]
                            if (character != null) {
                                CharacterItem(
                                    character = character,
                                    onClick = { /* TODO: Navigate to detail */ }
                                )
                            } else {
                                CharacterItemPlaceholder()
                            }
                        }
                    }


                    lazyCharacters.apply {
                        when {
                            // Начальная загрузка (refresh) обрабатывается PullRefreshIndicator
                            // поэтому FullScreenLoading() здесь не нужен если pull-to-refresh активен.
                            // Однако, если это не pull-to-refresh, а просто первая загрузка,
                            // то индикатор будет в центре.
                            // Если itemCount == 0 и isRefreshing (т.е. loadState.refresh is LoadState.Loading),
                            // то индикатор уже будет показан сверху.

                            loadState.append is LoadState.Loading -> {
                                item { LoadingNextPageIndicator() }
                            }

                            loadState.refresh is LoadState.Error -> {
                                // Если это ошибка при pull-to-refresh, индикатор исчезнет,
                                // и мы покажем это состояние на весь экран.
                                val e = loadState.refresh as LoadState.Error
                                item { // Используем span, чтобы ошибка занимала все колонки, если нужно
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
                                item { // Используем span, чтобы занимало все колонки
                                     EmptyState(message = "No characters found.")
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter) // Располагаем индикатор сверху по центру
                )
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
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = "Gender: ${character.gender}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CharacterItemPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.75f)) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FullScreenLoading() { // Этот компонент теперь менее вероятно будет виден при активном pull-to-refresh
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
    }
}

@Composable
fun LoadingNextPageIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
