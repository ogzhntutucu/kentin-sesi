package io.github.thwisse.kentinsesi.data.model

enum class PostStatus(val value: String) {
    NEW("new"),
    IN_PROGRESS("in_progress"),
    RESOLVED("resolved"),
    REJECTED("rejected");
    
    companion object {
        fun fromString(value: String): PostStatus {
            return values().find { it.value == value } ?: NEW
        }
    }
}

