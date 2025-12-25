package io.github.thwisse.kentinsesi.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthException
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * AuthRepository interface'inin Firebase ile çalışan somut implementasyonu
 * 
 * Artık Resource<T> kullanıyor - Diğer repository'lerle tutarlılık için
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun loginUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: FirebaseAuthException) {
            // Firebase özel hata mesajlarını kullan
            Resource.Error(getAuthErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Giriş yapılırken bir hata oluştu")
        }
    }

    override suspend fun registerUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: FirebaseAuthException) {
            // Firebase özel hata mesajlarını kullan
            Resource.Error(getAuthErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kayıt olurken bir hata oluştu")
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun signOut() {
        auth.signOut()
    }
    
    /**
     * Firebase Auth hata kodlarını Türkçe mesajlara çevirir
     */
    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Geçersiz e-posta adresi"
            "ERROR_WRONG_PASSWORD" -> "Hatalı şifre"
            "ERROR_USER_NOT_FOUND" -> "Kullanıcı bulunamadı"
            "ERROR_USER_DISABLED" -> "Bu hesap devre dışı bırakılmış"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Bu e-posta adresi zaten kullanılıyor"
            "ERROR_WEAK_PASSWORD" -> "Şifre çok zayıf. En az 6 karakter olmalıdır"
            "ERROR_NETWORK_REQUEST_FAILED" -> "İnternet bağlantısı hatası"
            "ERROR_TOO_MANY_REQUESTS" -> "Çok fazla deneme. Lütfen daha sonra tekrar deneyin"
            else -> exception.message ?: "Bir hata oluştu"
        }
    }
}