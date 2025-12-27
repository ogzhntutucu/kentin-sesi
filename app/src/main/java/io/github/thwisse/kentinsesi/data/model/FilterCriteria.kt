package io.github.thwisse.kentinsesi.data.model

data class FilterCriteria(
    val districts: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val statuses: List<String> = emptyList()
)
