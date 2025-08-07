package org.sexyslave.app.di

import org.koin.dsl.module
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.api.CharacterApiImpl
import org.sexyslave.app.features.characters.mvi.CharactersViewModel

val appModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    single<CharacterApi> { CharacterApiImpl(get()) }



//    // Особенно важно: регистрация ViewModel
//    viewModel { CharactersViewModel(get()) }

    // ИЛИ для мультиплатформы/Compose:
    factory { CharactersViewModel(get()) }

}