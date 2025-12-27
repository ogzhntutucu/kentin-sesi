package io.github.thwisse.kentinsesi.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPresetDao {

    @Query(
        "SELECT * FROM filter_presets " +
            "ORDER BY isSystemDefault DESC, isDefault DESC, name COLLATE NOCASE ASC"
    )
    fun observeAll(): Flow<List<FilterPresetEntity>>

    @Query("SELECT * FROM filter_presets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FilterPresetEntity?

    @Query("SELECT * FROM filter_presets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): FilterPresetEntity?

    @Query("SELECT * FROM filter_presets WHERE isSystemDefault = 1 LIMIT 1")
    suspend fun getSystemDefault(): FilterPresetEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: FilterPresetEntity)

    @Query("DELETE FROM filter_presets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE filter_presets SET isDefault = 0")
    suspend fun clearDefaultFlag()

    @Query("UPDATE filter_presets SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultFlag(id: String)
}
