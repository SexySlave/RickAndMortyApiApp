package org.sexyslave.app.features.characters.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.CharacterApi

class CharactersViewModel(
    private val api: CharacterApi
) : ScreenModel {
    private var _state by mutableStateOf(CharactersState())
    val state: CharactersState
        get() = _state

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        screenModelScope.launch {
            _state = _state.copy(isLoading = true)
            try {
                val characters = api.getCharacters()
                _state = _state.copy(
                    characters = characters,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state = _state.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

data class CharactersState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)