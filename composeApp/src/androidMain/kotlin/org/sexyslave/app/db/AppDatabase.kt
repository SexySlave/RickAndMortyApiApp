package org.sexyslave.app.db // или ваш выбранный пакет

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.sexyslave.app.features.characters.data.local.converters.Converters
import org.sexyslave.app.features.characters.data.local.dao.CharacterDao
import org.sexyslave.app.features.characters.data.local.model.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao


}