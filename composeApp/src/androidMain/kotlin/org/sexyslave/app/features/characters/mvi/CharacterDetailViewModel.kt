package org.sexyslave.app.features.characters.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sexyslave.app.features.characters.data.CharacterRepository
import org.sexyslave.app.features.characters.data.api.Character

sealed interface CharacterDetailUiState {
    object Loading : CharacterDetailUiState
    data class Success(val character: Character) : CharacterDetailUiState
    data class Error(val message: String) : CharacterDetailUiState
}

class CharacterDetailViewModel(
    private val characterRepository: CharacterRepository,
    private val characterId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterDetailUiState>(CharacterDetailUiState.Loading)
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    init {
        fetchCharacterDetails()
    }

    fun fetchCharacterDetails() {
        _uiState.value = CharacterDetailUiState.Loading
        viewModelScope.launch {
            try {
                val character = characterRepository.getCharacterDetails(characterId)
                _uiState.value = CharacterDetailUiState.Success(character)
            } catch (e: Exception) {
                _uiState.value = CharacterDetailUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}