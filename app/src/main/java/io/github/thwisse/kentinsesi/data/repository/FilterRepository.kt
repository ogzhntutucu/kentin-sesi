package io.github.thwisse.kentinsesi.data.repository

import io.github.thwisse.kentinsesi.data.model.FilterCriteria
import io.github.thwisse.kentinsesi.data.model.FilterPreset
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.flow.Flow

interface FilterRepository {

    fun observePresets(): Flow<List<FilterPreset>>

    suspend fun ensureSystemDefaultExists(): Resource<Unit>

    suspend fun savePreset(name: String, criteria: FilterCriteria): Resource<Unit>

    suspend fun deletePreset(id: String): Resource<Unit>

    suspend fun setDefaultPreset(id: String): Resource<Unit>

    suspend fun getDefaultPreset(): FilterPreset?

    suspend fun getPresetById(id: String): FilterPreset?

    fun observeLastAppliedPresetId(): Flow<String?>

    suspend fun setLastAppliedPresetId(id: String?): Resource<Unit>

    fun observeLastCriteria(): Flow<FilterCriteria?>

    suspend fun setLastCriteria(criteria: FilterCriteria?): Resource<Unit>
}
