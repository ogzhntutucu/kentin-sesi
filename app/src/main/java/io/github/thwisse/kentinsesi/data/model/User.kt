package io.github.thwisse.kentinsesi.data.model

import com.google.firebase.firestore.Exclude
import io.github.thwisse.kentinsesi.util.Constants

/**
 * User modeli - Uygulama kullanıcılarını temsil eder
 * 
 * NOT: Firestore string olarak sakladığı için role'u String olarak tutuyoruz,
 * ama enum'a çevirmek için roleEnum property'si ekledik
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val username: String = "", // @kullaniciadi gibi (isteğe bağlı)
    val city: String = "",     // Örn: Hatay
    val district: String = "", // Örn: İskenderun
    val avatarSeed: String = "", // DiceBear avatar seed (UUID format)
    
    // NOT: Firestore string olarak sakladığı için String kullanıyoruz
    // Ama enum'a çevirmek için roleEnum property'si ekledik
    val role: String = UserRole.CITIZEN.value, // Varsayılan: "citizen"
    val points: Long = 0,
    val title: String = Constants.TITLE_NEW_USER // Varsayılan: "Yeni Kullanıcı"
) {
    /**
     * Role'u enum olarak döndürür - Kod içinde kullanım için
     * Örnek: if (user.roleEnum == UserRole.OFFICIAL) { ... }
     */
    @get:Exclude
    val roleEnum: UserRole
        get() = UserRole.fromString(role)
    
    /**
     * Kullanıcının yetkili olup olmadığını kontrol eder
     */
    @get:Exclude
    val isOfficial: Boolean
        get() = roleEnum == UserRole.OFFICIAL || roleEnum == UserRole.ADMIN
    
    /**
     * Kullanıcının admin olup olmadığını kontrol eder
     */
    @get:Exclude
    val isAdmin: Boolean
        get() = roleEnum == UserRole.ADMIN
}