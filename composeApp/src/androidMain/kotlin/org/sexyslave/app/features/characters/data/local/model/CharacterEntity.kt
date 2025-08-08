package org.sexyslave.app.features.characters.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.sexyslave.app.features.characters.data.api.Character
import org.sexyslave.app.features.characters.data.api.Origin
import org.sexyslave.app.features.characters.data.api.Location

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val image: String,
    val episodeUrls: List<String>,
    val url: String,
    val created: String,
    var displayOrder: Int = 0
)

fun Character.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type ?: "",
        gender = this.gender,
        originName = this.origin.name,
        originUrl = this.origin.url,
        locationName = this.location.name,
        locationUrl = this.location.url,
        image = this.image,
        episodeUrls = this.episode,
        url = this.url,
        created = this.created
    )
}

fun CharacterEntity.toDomainModel(): Character {
    return Character(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        origin = Origin(name = this.originName, url = this.originUrl),
        location = Location(name = this.locationName, url = this.locationUrl),
        image = this.image,
        episode = this.episodeUrls,
        url = this.url,
        created = this.created
    )
}
