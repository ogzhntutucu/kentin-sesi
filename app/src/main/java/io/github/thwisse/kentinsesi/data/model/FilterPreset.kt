package io.github.thwisse.kentinsesi.data.model

data class FilterPreset(
    val id: String,
    val name: String,
    val criteria: FilterCriteria,
    val isSystemDefault: Boolean,
    val isDefault: Boolean
)
