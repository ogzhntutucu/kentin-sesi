package io.github.thwisse.kentinsesi.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.github.thwisse.kentinsesi.util.Resource

/**
 * Authentication işlemleri için sözleşme (interface)
 * 
 * Tüm metodlar Resource<T> döndürür - Diğer repository'lerle tutarlılık için
 */
interface AuthRepository {

    // Mevcut giriş yapmış kullanıcıyı döndürür (varsa)
    val currentUser: FirebaseUser?

    // E-posta ve şifre ile giriş yapma
    suspend fun loginUser(email: String, password: String): Resource<AuthResult>

    // Yeni kullanıcı kaydetme
    suspend fun registerUser(email: String, password: String): Resource<AuthResult>

    // Çıkış yapma
    fun logout()

    fun signOut()
}