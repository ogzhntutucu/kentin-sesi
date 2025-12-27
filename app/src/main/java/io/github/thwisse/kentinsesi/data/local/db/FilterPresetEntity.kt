package io.github.thwisse.kentinsesi.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "filter_presets",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class FilterPresetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val districts: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val statuses: List<String> = emptyList(),
    val onlyMyPosts: Boolean = false,
    val isSystemDefault: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
