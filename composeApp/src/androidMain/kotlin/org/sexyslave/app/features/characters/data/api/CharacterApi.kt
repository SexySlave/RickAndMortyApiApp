package org.sexyslave.app.features.characters.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

interface CharacterApi {
    suspend fun getCharacters(): List<Character>
}

class CharacterApiImpl(private val client: HttpClient) : CharacterApi {
    override suspend fun getCharacters(): List<Character> {
        // Deserialize into the new CharacterApiResponse
        val response = client.get("https://rickandmortyapi.com/api/character").body<CharacterApiResponse>()
        // Return the list of characters from the 'results' field
        return response.results
    }
}

// New data class to represent the full API response
@Serializable
data class CharacterApiResponse(
    val info: Info, // You might want to define the Info class if you need pagination details
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
)
