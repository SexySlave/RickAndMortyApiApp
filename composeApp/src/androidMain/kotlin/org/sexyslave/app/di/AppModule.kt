package org.sexyslave.app.di


import androidx.room.Room
import org.koin.dsl.module
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.sexyslave.app.db.AppDatabase
import org.sexyslave.app.features.characters.data.CharacterRepository
import org.sexyslave.app.features.characters.data.CharacterRepositoryImpl
import org.sexyslave.app.features.characters.data.api.CharacterApi
import org.sexyslave.app.features.characters.data.api.CharacterApiImpl
import org.sexyslave.app.features.characters.mvi.CharacterDetailViewModel
import org.sexyslave.app.features.characters.mvi.CharactersViewModel



val appModule = module {
    // Сетевые зависимости
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

    // Репозиторий
    single<CharacterRepository> {
        CharacterRepositoryImpl(
            characterApi = get(),
            database = get()
        )
    }

    // CharactersViewModel
     factory { CharactersViewModel(characterRepository = get()) }

    // CharacterDetailViewModel
    viewModel { (characterId: Int) ->
        CharacterDetailViewModel(
            characterRepository = get(),
            characterId = characterId
        )
    }


    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "rick_and_morty_db"
        )
            // .addMigrations(...)
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        val database = get<AppDatabase>()
        database.characterDao()
    }



}
