package org.sexyslave.app.features.characters.ui


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.mvi.CharactersViewModel
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import org.sexyslave.app.features.characters.data.CacheRefreshState

object CharactersScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: CharactersViewModel = getScreenModel()
        val lazyCharacters: LazyPagingItems<Character> = viewModel.charactersFlow.collectAsLazyPagingItems()
        val cacheState by viewModel.cacheRefreshState.collectAsState()

        // isRefreshing теперь зависит от cacheState, а не от Paging LoadState
        val isActuallyRefreshing = cacheState is CacheRefreshState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isActuallyRefreshing,
            onRefresh = { viewModel.refreshCache() }
        )

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(cacheState) {
            if (cacheState is CacheRefreshState.Error) {
                snackbarHostState.showSnackbar(
                    message = (cacheState as CacheRefreshState.Error).message,
                    duration = androidx.compose.material3.SnackbarDuration.Long
                )
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(title = { Text("Rick and Morty Characters") })
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                // Основная логика отображения контента
                when {
                    // 1. Полноэкранный лоадер, если список от Paging пуст И кеш активно обновляется
                    lazyCharacters.itemCount == 0 && isActuallyRefreshing && lazyCharacters.loadState.refresh is LoadState.Loading -> {
                        FullScreenLoading()
                    }
                    // 2. Ошибка от Paging (если PagingSource из DAO вернул ошибку при первоначальной загрузке)
                    // И при этом кеш не обновляется и не было ошибки обновления кеша (чтобы не перекрывать ошибку сети)
                    lazyCharacters.loadState.refresh is LoadState.Error && lazyCharacters.itemCount == 0 && !isActuallyRefreshing && cacheState !is CacheRefreshState.Error -> {
                        val error = lazyCharacters.loadState.refresh as LoadState.Error
                        ErrorState(

                            message = "Error from local data: ${error.error.localizedMessage}",
                            onRetry = { lazyCharacters.retry() }
                        )
                    }
                    // 3. Список персонажей (даже если пустой, LazyVerticalGrid справится)
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyGridState()
                        ) {
                            // Отображение элементов списка
                            if (lazyCharacters.itemCount > 0) {
                                items(
                                    count = lazyCharacters.itemCount,
                                    key = { index -> lazyCharacters.peek(index)?.id ?: -1 }
                                ) { index ->
                                    val character = lazyCharacters[index]
                                    if (character != null) {
                                        CharacterItem(
                                            character = character,
                                            onClick = {
                                                navigator.push(CharacterDetailScreen(character.id))
                                            }
                                        )
                                    } else {
                                        // Плейсхолдер, если enablePlaceholders = true в PagingConfig
                                        CharacterItemPlaceholder()
                                    }
                                }
                            }

                            // Состояния загрузки и ошибок от Paging для APPEND (подгрузка следующих страниц из БД)
                            when (lazyCharacters.loadState.append) {
                                is LoadState.Loading -> {
                                    // span: { GridItemSpan(maxLineSpan) }
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                        LoadingNextPageIndicator()
                                    }
                                }
                                is LoadState.Error -> {
                                    val e = lazyCharacters.loadState.append as LoadState.Error
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                        ErrorStateSmall(
                                            message = "Error loading more: ${e.error.localizedMessage}",
                                            onRetry = { lazyCharacters.retry() }
                                        )
                                    }
                                }
                                else -> {}
                            }

                            // Состояния загрузки и ошибок от Paging для REFRESH (первоначальная загрузка из БД)
                            // Это обрабатывается выше, но если itemCount > 0, можно показать маленькую ошибку здесь
                            if (lazyCharacters.loadState.refresh is LoadState.Error && lazyCharacters.itemCount > 0) {
                                val e = lazyCharacters.loadState.refresh as LoadState.Error
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    ErrorStateSmall( // Используем маленькую ошибку, т.к. данные уже есть
                                        message = "Refresh error: ${e.error.localizedMessage}",
                                        onRetry = { lazyCharacters.refresh() } // Используем refresh() для Paging
                                    )
                                }
                            }

                            // Пустое состояние: если Paging не загружает, кеш не обновляется, ошибок нет, и itemCount == 0
                            if (lazyCharacters.loadState.refresh is LoadState.NotLoading &&
                                lazyCharacters.loadState.append.endOfPaginationReached && // Убедимся, что Paging завершил все
                                lazyCharacters.itemCount == 0 &&
                                !isActuallyRefreshing &&
                                cacheState !is CacheRefreshState.Error
                            ) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    EmptyState(message = "No characters found.")
                                }
                            }
                        }
                    }
                }

                // PullRefreshIndicator всегда поверх
                PullRefreshIndicator(
                    refreshing = isActuallyRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)

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
