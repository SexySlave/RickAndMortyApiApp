package org.sexyslave.app.di

import org.koin.dsl.module
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel // <--- ВАЖНО: Импорт для viewModel DSL
import org.koin.core.module.dsl.factoryOf // Для ScreenModel
import org.sexyslave.app.features.characters.data.CharacterRepository
import org.sexyslave.app.features.characters.data.CharacterRepositoryImpl
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.api.CharacterApiImpl
import org.sexyslave.app.features.characters.mvi.CharacterDetailViewModel // <--- Импортируйте CharacterDetailViewModel
import org.sexyslave.app.features.characters.mvi.CharactersViewModel

val appModule = module {
    // Сетевые зависимости
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    // prettyPrint = true // удобно для отладки JSON ответов
                })
            }
        }
    }
    single<CharacterApi> { CharacterApiImpl(get()) }

    // Репозиторий
    single<CharacterRepository> { CharacterRepositoryImpl(get()) } // <--- ДОБАВЛЕНО ОПРЕДЕЛЕНИЕ РЕПОЗИТОРИЯ

    // ViewModel'и
    // CharactersViewModel (это ScreenModel, поэтому используется factory или factoryOf)
    factoryOf(::CharactersViewModel) // Если CharactersViewModel не принимает параметров, это ок.
    // Если принимает characterApi, то factory { CharactersViewModel(get()) }

    // CharacterDetailViewModel (это AndroidX ViewModel)
    viewModel { (characterId: Int) -> // <--- ДОБАВЛЕНО ОПРЕДЕЛЕНИЕ ДЛЯ CharacterDetailViewModel
        CharacterDetailViewModel(
            characterRepository = get(), // Koin предоставит CharacterRepository
            characterId = characterId      // characterId будет передан из parametersOf()
        )
    }
}
