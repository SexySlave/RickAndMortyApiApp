package org.sexyslave.app.features.characters.mvi

import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.sexyslave.app.features.characters.data.CharacterRepository
import org.sexyslave.app.features.characters.data.api.Character
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.sexyslave.app.features.characters.data.CacheRefreshState

class CharactersViewModel(
    private val characterRepository: CharacterRepository
) : ScreenModel {

    // Поток данных для UI, читает из локальной БД (DAO)
    val charactersFlow: Flow<PagingData<Character>> =
        characterRepository.getCharactersStream()
            .cachedIn(screenModelScope) // кеширование PagingData в scope ViewModel

    // Состояние процесса обновления кеша
    val cacheRefreshState: StateFlow<CacheRefreshState> = characterRepository.cacheRefreshState
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Начинаем сбор, когда есть подписчики
            initialValue = CacheRefreshState.Idle
        )

    init {
        // Запускаем начальное обновление кеша при создании ViewModel,
        // но только если оно еще не запущено или не было успешно завершено недавно.
        refreshCache()
    }

    fun refreshCache() {
        // Проверяем, не идет ли уже загрузка, чтобы не запускать параллельно с UI
        if (cacheRefreshState.value != CacheRefreshState.Loading) {
            screenModelScope.launch {
                characterRepository.refreshCharacterCache()
            }
        }
    }

}