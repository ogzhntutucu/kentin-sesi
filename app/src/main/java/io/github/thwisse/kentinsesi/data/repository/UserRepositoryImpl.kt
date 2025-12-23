package io.github.thwisse.kentinsesi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import io.github.thwisse.kentinsesi.data.model.User
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // 1. Kullanıcı Profili Oluşturma
    override suspend fun createUserProfile(uid: String, fullName: String, email: String): Resource<Unit> {
        return try {
            val newUser = User(
                uid = uid,
                fullName = fullName,
                email = email,
                role = "citizen"
            )
            firestore.collection(USERS_COLLECTION).document(uid).set(newUser).await()
            // Düzeltme: Result.success yerine Resource.Success kullanıyoruz
            Resource.Success(Unit)
        } catch (e: Exception) {
            // Düzeltme: Result.failure yerine Resource.Error kullanıyoruz
            Resource.Error(e.message ?: "Profil oluşturulamadı.")
        }
    }

    // 2. Kullanıcı Profili Güncelleme (YENİ EKLENEN)
    override suspend fun updateUserProfile(uid: String, fullName: String, city: String, district: String): Resource<Unit> {
        return try {
            val updates = mapOf(
                "fullName" to fullName,
                "city" to city,
                "district" to district,
                "title" to "Duyarlı Vatandaş"
            )

            firestore.collection(USERS_COLLECTION).document(uid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profil güncellenemedi.")
        }
    }
}