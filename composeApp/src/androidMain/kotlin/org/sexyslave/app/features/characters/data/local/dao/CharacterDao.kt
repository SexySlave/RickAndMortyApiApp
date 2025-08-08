package org.sexyslave.app.features.characters.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.sexyslave.app.features.characters.data.local.model.CharacterEntity

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    // Этот PagingSource будет использоваться Pager для загрузки данных из БД
    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: Int): CharacterEntity?

    @Query("DELETE FROM characters")
    suspend fun clearAll()


    @Query("SELECT MAX(id) FROM characters")
    suspend fun getLastCharacterId(): Int?


    @Query(
        """
        SELECT * FROM characters 
        WHERE 
            (:name IS NULL OR name LIKE '%' || :name || '%') AND
            (:status IS NULL OR status = :status) AND
            (:species IS NULL OR species LIKE '%' || :species || '%') AND
            (:type IS NULL OR type LIKE '%' || :type || '%') AND
            (:gender IS NULL OR gender = :gender)
        ORDER BY id ASC 
        """
    )
    fun getFilteredPagingSource(
        name: String?,
        status: String?,
        species: String?,
        type: String?,
        gender: String?
    ): PagingSource<Int, CharacterEntity>
}