package io.github.thwisse.kentinsesi.util

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }
    
    fun isValidPostTitle(title: String): Boolean {
        return title.isNotBlank() && title.length <= Constants.MAX_POST_TITLE_LENGTH
    }
    
    fun isValidPostDescription(description: String): Boolean {
        return description.isNotBlank() && description.length <= Constants.MAX_POST_DESCRIPTION_LENGTH
    }
    
    fun isValidComment(comment: String): Boolean {
        return comment.isNotBlank() && comment.length <= Constants.MAX_COMMENT_LENGTH
    }
    
    fun getValidationError(field: String, value: String): String? {
        return when (field) {
            "email" -> if (!isValidEmail(value)) "Geçerli bir e-posta adresi giriniz" else null
            "password" -> if (!isValidPassword(value)) "Şifre en az ${Constants.MIN_PASSWORD_LENGTH} karakter olmalıdır" else null
            "title" -> if (!isValidPostTitle(value)) "Başlık boş olamaz ve ${Constants.MAX_POST_TITLE_LENGTH} karakteri geçemez" else null
            "description" -> if (!isValidPostDescription(value)) "Açıklama boş olamaz ve ${Constants.MAX_POST_DESCRIPTION_LENGTH} karakteri geçemez" else null
            "comment" -> if (!isValidComment(value)) "Yorum boş olamaz ve ${Constants.MAX_COMMENT_LENGTH} karakteri geçemez" else null
            else -> null
        }
    }
}

