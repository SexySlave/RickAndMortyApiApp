package org.sexyslave.app.features.characters.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.paging.CharactersPagingSource

interface CharacterRepository {
    fun getCharactersStream(): Flow<PagingData<Character>>
}

class CharacterRepositoryImpl(
    private val characterApi: CharacterApi
) : CharacterRepository {
    override fun getCharactersStream(): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // Стандартный размер страницы для этого API
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CharactersPagingSource(characterApi) }
        ).flow
    }
}