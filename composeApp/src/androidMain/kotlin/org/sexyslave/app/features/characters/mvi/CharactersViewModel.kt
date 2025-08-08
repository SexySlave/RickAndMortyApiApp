package org.sexyslave.app.features.characters.mvi

import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.sexyslave.app.features.characters.data.CharacterRepository
import org.sexyslave.app.features.characters.data.api.Character
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.sexyslave.app.features.characters.data.CacheRefreshState
import org.sexyslave.app.features.characters.data.CharacterFilters

class CharactersViewModel(
    private val characterRepository: CharacterRepository
) : ScreenModel {


    private val _currentFilters =
        MutableStateFlow(CharacterFilters()) // Изначально фильтры сброшены
    val currentFilters: StateFlow<CharacterFilters> = _currentFilters.asStateFlow()


    // flatMapLatest будет переключать источник PagingData при изменении фильтров
    val charactersFlow: Flow<PagingData<Character>> = currentFilters.flatMapLatest { filters ->
        if (filters.isClear()) {
            characterRepository.getCharactersStream()
        } else {
            characterRepository.getFilteredCharactersStream(filters)
        }
    }.cachedIn(screenModelScope)

    val cacheRefreshState: StateFlow<CacheRefreshState> = characterRepository.cacheRefreshState
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CacheRefreshState.Idle
        )

    init {
        refreshCache()
    }

    fun refreshCache() {
        if (cacheRefreshState.value != CacheRefreshState.Loading) {
            screenModelScope.launch {
                characterRepository.refreshCharacterCache()
            }
        }
    }


    fun applyFilters(newFilters: CharacterFilters) {
        _currentFilters.value = newFilters
    }

    fun clearFilters() {
        _currentFilters.value = CharacterFilters() // Сброс на дефолтные (пустые) фильтры
    }

    override fun onDispose() {
        super.onDispose()
    }
}