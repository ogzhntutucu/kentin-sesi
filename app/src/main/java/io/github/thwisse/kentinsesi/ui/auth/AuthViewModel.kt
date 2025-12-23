package io.github.thwisse.kentinsesi.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.thwisse.kentinsesi.data.repository.AuthRepository
import io.github.thwisse.kentinsesi.data.repository.UserRepository
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI Durumları
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val authResult: AuthResult) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // --- Giriş Durumu ---
    private val _loginState = MutableLiveData<AuthState>(AuthState.Idle)
    val loginState: LiveData<AuthState> = _loginState

    // --- Kayıt Durumu ---
    private val _registrationState = MutableLiveData<AuthState>(AuthState.Idle)
    val registrationState: LiveData<AuthState> = _registrationState

    // --- Profil Güncelleme Durumu (YENİ) ---
    // Resource<Unit> kullanıyoruz çünkü geriye veri dönmüyor, sadece başarı/hata önemli.
    private val _updateProfileState = MutableLiveData<Resource<Unit>>()
    val updateProfileState: LiveData<Resource<Unit>> = _updateProfileState

    // GİRİŞ YAP
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = authRepository.loginUser(email, password)
            result.onSuccess { authResult ->
                _loginState.value = AuthState.Success(authResult)
            }.onFailure { exception ->
                _loginState.value = AuthState.Error(exception.message ?: "Giriş hatası")
            }
        }
    }

    // KAYIT OL (Düzeltildi: İsim parametresi kalktı ve Resource tipi düzeltildi)
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = AuthState.Loading

            // 1. Auth İşlemi (Hala Result dönüyor)
            val authResult = authRepository.registerUser(email, password)

            authResult.onSuccess { result ->
                val user = result.user
                if (user != null) {
                    // 2. Profil Oluşturma (Artık Resource dönüyor)
                    // İsmi şimdilik boş ("") gönderiyoruz.
                    val profileResource = userRepository.createUserProfile(user.uid, "", email)

                    // Resource kontrolü (onSuccess yerine when kullanıyoruz)
                    when (profileResource) {
                        is Resource.Success -> {
                            _registrationState.value = AuthState.Success(result)
                        }
                        is Resource.Error -> {
                            _registrationState.value = AuthState.Error(profileResource.message ?: "Profil hatası")
                        }
                        is Resource.Loading -> {
                            // Loading durumu zaten başta set edildi
                        }
                    }
                } else {
                    _registrationState.value = AuthState.Error("Kullanıcı verisi alınamadı.")
                }
            }.onFailure { exception ->
                _registrationState.value = AuthState.Error(exception.message ?: "Kayıt hatası")
            }
        }
    }

    // PROFİL TAMAMLAMA (YENİ)
    fun completeProfile(fullName: String, city: String, district: String) {
        viewModelScope.launch {
            // Resource.Loading<Unit> olarak tip belirtiyoruz (Hata: Cannot infer type çözümü)
            _updateProfileState.value = Resource.Loading()

            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                val result = userRepository.updateUserProfile(currentUser.uid, fullName, city, district)
                _updateProfileState.value = result
            } else {
                // Resource.Error<Unit> olarak tip belirtiyoruz
                _updateProfileState.value = Resource.Error("Kullanıcı oturumu bulunamadı.")
            }
        }
    }
}