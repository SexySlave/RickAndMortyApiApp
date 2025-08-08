package org.sexyslave.app.features.characters.data


enum class CharacterStatus(val apiValue: String) {
    ALIVE("Alive"),
    DEAD("Dead"),
    UNKNOWN("Unknown");

    companion object {
        fun fromApiValue(value: String?): CharacterStatus? = entries.find { it.apiValue.equals(value, ignoreCase = true) }
    }
}

enum class CharacterGender(val apiValue: String) {
    FEMALE("Female"),
    MALE("Male"),
    GENDERLESS("Genderless"),
    UNKNOWN("Unknown");

    companion object {
        fun fromApiValue(value: String?): CharacterGender? = entries.find { it.apiValue.equals(value, ignoreCase = true) }
    }
}

data class CharacterFilters(
    val name: String? = null,
    val status: CharacterStatus? = null,
    val species: String? = null,
    val type: String? = null,
    val gender: CharacterGender? = null
) {
    fun isClear(): Boolean {
        return name.isNullOrBlank() && status == null && species.isNullOrBlank() && type.isNullOrBlank() && gender == null
    }
}