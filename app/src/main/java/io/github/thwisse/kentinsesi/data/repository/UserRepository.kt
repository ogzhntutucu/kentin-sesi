package io.github.thwisse.kentinsesi.data.repository

import io.github.thwisse.kentinsesi.data.model.User
import io.github.thwisse.kentinsesi.util.Resource

// Kullanıcı profili işlemleri için sözleşme
interface UserRepository {
    // ... mevcut createUserProfile fonksiyonu burada kalsın ...

    suspend fun createUserProfile(uid: String, fullName: String, email: String): Resource<Unit>

    // --- BU FONKSİYONU EKLE ---
    // Kullanıcının eksik bilgilerini (isim, il, ilçe) tamamlamak için
    suspend fun updateUserProfile(uid: String, fullName: String, city: String, district: String): Resource<Unit>
}