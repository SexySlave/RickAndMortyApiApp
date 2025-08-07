package org.sexyslave.app.features.characters.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class CharacterApiResponse(
    val info: Info,
    val results: List<Character>
)

@Serializable
data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

@Serializable
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
    // Добавьте сюда origin и location, если они нужны сразу в списке
    // val origin: Origin,
    // val location: Location
)

// @Serializable
// data class Origin(
//    val name: String,
//    val url: String
// )

// @Serializable
// data class Location(
//    val name: String,
//    val url: String
// )

interface CharacterApi {
    suspend fun getCharacters(page: Int): CharacterApiResponse
}

class CharacterApiImpl(private val client: HttpClient) : CharacterApi {
    private val baseUrl = "https://rickandmortyapi.com/api/character"

    override suspend fun getCharacters(page: Int): CharacterApiResponse {
        return client.get(baseUrl) {
            parameter("page", page)
        }.body()
    }
}
