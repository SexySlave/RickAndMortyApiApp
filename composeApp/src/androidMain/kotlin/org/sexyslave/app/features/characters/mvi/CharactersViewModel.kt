package org.sexyslave.app.features.characters.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.sexyslave.app.features.characters.data.CharacterRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineScope // <--- Импорт для CoroutineScope
import kotlinx.coroutines.coroutineScope
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.CharacterApi



import androidx.paging.cachedIn // Убедитесь, что это androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.screenModelScope // Правильный scope для ScreenModel
import org.sexyslave.app.features.characters.data.paging.CharactersPagingSource // <--- ВАЖНО: Добавьте этот импорт


class CharactersViewModel(
    private val characterApi: CharacterApi
) : ScreenModel {

    val charactersFlow: Flow<PagingData<Character>> = Pager(
        // Pager автоматически выведет Key (вероятно, Int) и Value (Character)
        // из вашего CharactersPagingSource после того, как он будет распознан.
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { CharactersPagingSource(characterApi) } // <--- Должен разрешиться после импорта
    ).flow
        .cachedIn(screenModelScope) // <--- Используйте screenModelScope

    override fun onDispose() {
        super.onDispose()
        // Логика очистки, если необходима
    }
}