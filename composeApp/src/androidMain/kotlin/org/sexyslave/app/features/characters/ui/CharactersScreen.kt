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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
        val currentFilters by viewModel.currentFilters.collectAsState() // Собираем текущие фильтры

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
                TopAppBar(
                    title = { Text("Rick and Morty Characters") },
                    actions = { // <--- СЕКЦИЯ ДЛЯ КНОПОК ДЕЙСТВИЙ
                        IconButton(onClick = {
                            // Навигация на экран фильтров:
                            navigator.push(
                                CharacterFiltersScreen(
                                    initialFilters = currentFilters, // Передаем текущие фильтры
                                    onApplyFilters = { newFilters ->
                                        viewModel.applyFilters(newFilters)
                                    },
                                    onClearFilters = {
                                        viewModel.clearFilters()
                                    }
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Filter Characters",
                                tint = if (currentFilters.isClear()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    lazyCharacters.itemCount == 0 && isActuallyRefreshing && lazyCharacters.loadState.refresh is LoadState.Loading -> {
                        FullScreenLoading(modifier = Modifier.align(Alignment.Center))
                    }
                    lazyCharacters.loadState.refresh is LoadState.Error && lazyCharacters.itemCount == 0 && !isActuallyRefreshing && cacheState !is CacheRefreshState.Error -> {
                        val error = lazyCharacters.loadState.refresh as LoadState.Error
                        ErrorState(
                            modifier = Modifier.align(Alignment.Center),
                            message = "Error from local data: ${error.error.localizedMessage}",
                            onRetry = { lazyCharacters.retry() }
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyGridState()
                        ) {
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
                                        CharacterItemPlaceholder()
                                    }
                                }
                            }

                            when (lazyCharacters.loadState.append) {
                                is LoadState.Loading -> {
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

                            if (lazyCharacters.loadState.refresh is LoadState.Error && lazyCharacters.itemCount > 0) {
                                val e = lazyCharacters.loadState.refresh as LoadState.Error
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    ErrorStateSmall(
                                        message = "Refresh error: ${e.error.localizedMessage}",
                                        onRetry = { lazyCharacters.refresh() }
                                    )
                                }
                            }

                            if (lazyCharacters.loadState.refresh is LoadState.NotLoading &&
                                lazyCharacters.loadState.append.endOfPaginationReached &&
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
    Card(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(0.75f)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
fun ErrorState(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier
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
fun ErrorStateSmall(modifier: Modifier = Modifier,message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier
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
fun EmptyState(modifier: Modifier = Modifier, message: String) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall)
    }
}
