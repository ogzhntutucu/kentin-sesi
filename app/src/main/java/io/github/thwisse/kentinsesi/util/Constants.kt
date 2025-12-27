package io.github.thwisse.kentinsesi.util

object Constants {
    // Firestore Collections
    const val COLLECTION_POSTS = "posts"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_COMMENTS = "comments"
    
    // Storage Paths
    const val STORAGE_POST_IMAGES = "post_images"
    
    // User Roles
    const val ROLE_CITIZEN = "citizen"
    const val ROLE_OFFICIAL = "official"
    const val ROLE_ADMIN = "admin"
    
    // Post Status
    const val STATUS_NEW = "new"
    const val STATUS_IN_PROGRESS = "in_progress"
    const val STATUS_RESOLVED = "resolved"
    const val STATUS_REJECTED = "rejected"
    
    // Post Categories
    const val CATEGORY_INFRASTRUCTURE = "infrastructure"
    const val CATEGORY_WASTE = "waste"
    const val CATEGORY_LIGHTING = "lighting"
    const val CATEGORY_TRAFFIC = "traffic"
    const val CATEGORY_OTHER = "other"
    
    // User Titles (Gamification)
    const val TITLE_NEW_USER = "Yeni Kullanıcı"
    const val TITLE_SENSITIVE_CITIZEN = "Duyarlı Vatandaş"
    const val TITLE_ACTIVE_CITIZEN = "Aktif Vatandaş"
    const val TITLE_CHAMPION = "Şampiyon Vatandaş"
    
    // Points System
    const val POINTS_POST_CREATED = 10L
    const val POINTS_POST_UPVOTED = 1L
    const val POINTS_POST_RESOLVED = 50L
    const val POINTS_COMMENT_ADDED = 2L
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_POST_TITLE_LENGTH = 100
    const val MAX_POST_DESCRIPTION_LENGTH = 500
    const val MAX_COMMENT_LENGTH = 200

    // Comments
    const val MAX_COMMENT_DEPTH = 4
    
    // Pagination
    const val POSTS_PAGE_SIZE = 20
    
    // Image
    const val MAX_IMAGE_SIZE_MB = 5
    const val IMAGE_COMPRESSION_QUALITY = 85
}

