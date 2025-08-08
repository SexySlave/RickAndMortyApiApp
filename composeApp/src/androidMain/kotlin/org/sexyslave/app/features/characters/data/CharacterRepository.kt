package org.sexyslave.app.features.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.sexyslave.app.features.characters.data.api.Character
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.sexyslave.app.db.AppDatabase
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.api.CharacterApiResponse
import org.sexyslave.app.features.characters.data.local.model.toDomainModel
import org.sexyslave.app.features.characters.data.local.model.toEntity
import java.io.IOException

// Sealed interface для состояний обновления кеша
sealed interface CacheRefreshState {
    object Idle : CacheRefreshState
    object Loading : CacheRefreshState
    data class Error(val message: String) : CacheRefreshState
    object Success : CacheRefreshState
}

interface CharacterRepository {
    fun getCharactersStream(): Flow<PagingData<Character>> // Для UI, читает из DAO
    suspend fun getCharacterDetails(id: Int): Character     // Для деталей, кеш-first

    val cacheRefreshState: StateFlow<CacheRefreshState>
    suspend fun refreshCharacterCache()
}

@OptIn(ExperimentalPagingApi::class)
class CharacterRepositoryImpl(
    private val characterApi: CharacterApi,
    private val database: AppDatabase
) : CharacterRepository {

    private val characterDao = database.characterDao()

    // Состояние обновления кеша
    private val _cacheRefreshState = MutableStateFlow<CacheRefreshState>(CacheRefreshState.Idle)
    override val cacheRefreshState: StateFlow<CacheRefreshState> = _cacheRefreshState.asStateFlow()

    // Этот Pager всегда читает из локальной базы данных.
    // Он автоматически обновится, когда refreshCharacterCache() изменит данные в БД.
    override fun getCharactersStream(): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // Не влияет на сеть, только на чтение из БД для UI
                enablePlaceholders = false
            ),

            pagingSourceFactory = { characterDao.pagingSource() }
        ).flow.map { pagingDataEntity ->
            pagingDataEntity.map { characterEntity ->
                characterEntity.toDomainModel()
            }
        }
    }

    override suspend fun refreshCharacterCache() {
        if (_cacheRefreshState.value == CacheRefreshState.Loading) {
            // Timber.d("Refresh already in progress")
            return // Не запускаем новый рефреш, если один уже идет
        }
        _cacheRefreshState.value = CacheRefreshState.Loading
        // Timber.d("Starting character cache refresh...")

        withContext(Dispatchers.IO) {
            try {
                val allCharactersFromApi = mutableListOf<Character>()
                var currentPage = 1
                var hasMorePages = true

                while (hasMorePages) {
                    // Timber.d("Fetching page: $currentPage")
                    val response: CharacterApiResponse = characterApi.getCharacters(page = currentPage)
                    allCharactersFromApi.addAll(response.results)

                    // Проверяем, есть ли следующая страница
                    hasMorePages = response.info.next != null
                    if (hasMorePages) {
                        currentPage++
                    }
                    // kotlinx.coroutines.delay(100) // Опциональная небольшая задержка
                }
                // Timber.d("Fetched ${allCharactersFromApi.size} characters from API.")

                // Сохраняем в базу данных в одной транзакции
                database.withTransaction {
                    characterDao.clearAll() // Очищаем старых персонажей
                    characterDao.insertAll(allCharactersFromApi.map { it.toEntity() })
                }
                _cacheRefreshState.value = CacheRefreshState.Success
                // Timber.d("Successfully refreshed character cache.")

            } catch (e: IOException) { // Ошибка сети
                _cacheRefreshState.value = CacheRefreshState.Error("Network error: ${e.localizedMessage ?: "Unknown network error"}")
                // Timber.e(e, "Network error refreshing cache")
            } catch (e: Exception) { // Другие ошибки (например, HTTP ошибки, если Ktor их выбрасывает, или ошибки сериализации)
                _cacheRefreshState.value = CacheRefreshState.Error("Failed to refresh cache: ${e.localizedMessage ?: "Unknown error"}")
                // Timber.e(e, "Error refreshing cache")
            }
        }
    }

    // Метод getCharacterDetails остается без изменений (сначала кеш, потом сеть)
    override suspend fun getCharacterDetails(id: Int): Character {
        return withContext(Dispatchers.IO) {
            val cachedCharacter = characterDao.getCharacterById(id)
            if (cachedCharacter != null) {
                cachedCharacter.toDomainModel()
            } else {
                // Timber.d("Character $id not in cache, fetching from API")
                val characterFromApi = characterApi.getCharacter(id)
                characterDao.insertCharacter(characterFromApi.toEntity())
                characterFromApi
            }
        }
    }
}