package org.sexyslave.app.features.characters.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow // Для простого Flow
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.paging.CharactersPagingSource

interface CharacterRepository {
    fun getCharactersStream(): Flow<PagingData<Character>>
    suspend fun getCharacterDetails(id: Int): Character // Новый метод
}

class CharacterRepositoryImpl(
    private val characterApi: CharacterApi
) : CharacterRepository {
    override fun getCharactersStream(): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CharactersPagingSource(characterApi) }
        ).flow
    }

    override suspend fun getCharacterDetails(id: Int): Character { // Реализация нового метода
        return characterApi.getCharacter(id)
    }
}
