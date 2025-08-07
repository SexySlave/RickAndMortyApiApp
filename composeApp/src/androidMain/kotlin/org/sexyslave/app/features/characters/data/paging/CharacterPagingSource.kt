package org.sexyslave.app.features.characters.data.paging // Или ваш актуальный пакет

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.CharacterApi

class CharactersPagingSource(
    private val characterApi: CharacterApi
) : PagingSource<Int, Character>() { // <--- Убедитесь, что типы Key (Int) и Value (Character) указаны

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val page = params.key ?: 1 // Начальная страница
        return try {
            val response = characterApi.getCharacters(page) // Предполагая, что ваш API метод принимает номер страницы
            LoadResult.Page(
                data = response.results, // Предполагая, что ваш API возвращает объект с полем results: List<Character>
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.results.isEmpty()) null else page + 1 // или используйте информацию о следующей странице из API
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}