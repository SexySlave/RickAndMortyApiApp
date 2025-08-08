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
    val type: String,
    val gender: String,
    val origin: Origin,
    val location: Location,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

@Serializable
data class Origin(
    val name: String,
    val url: String
)

@Serializable
data class Location(
    val name: String,
    val url: String
)

interface CharacterApi {
    suspend fun getCharacters(page: Int): CharacterApiResponse
    suspend fun getCharacter(id: Int): Character
}

class CharacterApiImpl(private val client: HttpClient) : CharacterApi {
    private val baseUrl = "https://rickandmortyapi.com/api/character"

    override suspend fun getCharacters(page: Int): CharacterApiResponse {
        return client.get(baseUrl) {
            parameter("page", page)
        }.body()
    }

    override suspend fun getCharacter(id: Int): Character {
        return client.get("$baseUrl/$id").body()
    }
}
