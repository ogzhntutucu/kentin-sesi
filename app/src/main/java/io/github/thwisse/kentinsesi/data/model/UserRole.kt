package io.github.thwisse.kentinsesi.data.model

enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    OFFICIAL("official"),
    ADMIN("admin");
    
    companion object {
        fun fromString(value: String): UserRole {
            return values().find { it.value == value } ?: CITIZEN
        }
    }
}

