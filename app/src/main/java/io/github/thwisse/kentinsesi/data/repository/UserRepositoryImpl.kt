package io.github.thwisse.kentinsesi.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.thwisse.kentinsesi.data.model.User
import io.github.thwisse.kentinsesi.data.model.UserRole
import io.github.thwisse.kentinsesi.util.Constants
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private fun normalizeUsername(input: String): String {
        return input.trim().removePrefix("@").trim().lowercase()
    }

    // 1. Kullanıcı Profili Oluşturma
    override suspend fun createUserProfile(uid: String, fullName: String, email: String): Resource<Unit> {
        return try {
            val newUser = User(
                uid = uid,
                fullName = fullName,
                email = email,
                // Enum kullanarak tip güvenli hale getirdik
                role = UserRole.CITIZEN.value // "citizen" yerine UserRole.CITIZEN.value
            )
            // Constants kullanarak collection adını merkezileştirdik
            firestore.collection(Constants.COLLECTION_USERS).document(uid).set(newUser).await()

            // Eski şemadan kalan gereksiz alanları temizle (varsa)
            firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update(
                    mapOf(
                        "admin" to FieldValue.delete(),
                        "official" to FieldValue.delete(),
                        "roleEnum" to FieldValue.delete()
                    )
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profil oluşturulamadı.")
        }
    }

    // 2. Kullanıcı Profili Güncelleme
    override suspend fun updateUserProfile(uid: String, fullName: String, username: String, city: String, district: String, avatarSeed: String): Resource<Unit> {
        return try {
            val normalized = normalizeUsername(username)
            val usernameRegex = Regex("^[a-z0-9_]{3,20}$")
            if (normalized.isBlank() || !usernameRegex.matches(normalized)) {
                return Resource.Error("Geçersiz kullanıcı adı")
            }

            val userRef = firestore.collection(Constants.COLLECTION_USERS).document(uid)
            val usernameRef = firestore.collection("usernames").document(normalized)

            firestore.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val existingUsername = userSnap.getString("username").orEmpty().trim().lowercase()

                if (existingUsername.isNotBlank() && existingUsername != normalized) {
                    throw IllegalStateException("Kullanıcı adı değiştirilemez")
                }

                if (existingUsername.isBlank()) {
                    val unameSnap = tx.get(usernameRef)
                    if (unameSnap.exists()) {
                        throw IllegalStateException("Bu kullanıcı adı zaten alınmış")
                    }

                    tx.set(
                        usernameRef,
                        mapOf(
                            "uid" to uid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                tx.set(
                    userRef,
                    mapOf(
                        "fullName" to fullName,
                        "username" to normalized,
                        "city" to city,
                        "district" to district,
                        "avatarSeed" to avatarSeed,  // EKLENDI
                        "title" to Constants.TITLE_SENSITIVE_CITIZEN,
                        "admin" to FieldValue.delete(),
                        "official" to FieldValue.delete(),
                        "roleEnum" to FieldValue.delete()
                    ),
                    SetOptions.merge()
                )

                null
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profil güncellenemedi.")
        }
    }
    
    // 3. Kullanıcı Bilgisini Getir
    override suspend fun getUser(uid: String): Resource<User> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    Resource.Error("Kullanıcı verisi okunamadı.")
                }
            } else {
                Resource.Error("Kullanıcı bulunamadı.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kullanıcı bilgisi alınamadı.")
        }
    }
    
    // 4. Kullanıcı Rolünü Getir
    override suspend fun getUserRole(uid: String): Resource<String> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            if (document.exists()) {
                // Enum'dan varsayılan değer kullanıyoruz
                val role = document.getString("role") ?: UserRole.CITIZEN.value
                Resource.Success(role)
            } else {
                Resource.Error("Kullanıcı bulunamadı.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kullanıcı rolü alınamadı.")
        }
    }
    
    // 5. Tüm kullanıcıları getir (Admin paneli için)
    override suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            // fullName boş olabilir, bu yüzden sıralama yapmadan çekiyoruz
            // Sonra Kotlin'de sıralayacağız
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { doc ->
                val user = doc.toObject(User::class.java)
                user?.copy(uid = doc.id) // UID'yi document ID'den al
            }.sortedBy { it.fullName.ifEmpty { it.email } } // fullName yoksa email'e göre sırala
            
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kullanıcılar alınamadı.")
        }
    }

    override suspend fun getPrivilegedUsers(): Resource<List<User>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereIn("role", listOf(UserRole.ADMIN.value, UserRole.OFFICIAL.value))
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            }

            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kullanıcılar alınamadı.")
        }
    }

    override suspend fun searchUserByUsername(username: String): Resource<User> {
        return try {
            val normalized = normalizeUsername(username)
            val usernameDoc = firestore.collection("usernames").document(normalized).get().await()
            if (!usernameDoc.exists()) {
                return Resource.Error("Böyle bir kullanıcı yok")
            }

            val uid = usernameDoc.getString("uid").orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Böyle bir kullanıcı yok")
            }

            val userDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user == null) {
                Resource.Error("Böyle bir kullanıcı yok")
            } else {
                Resource.Success(user.copy(uid = userDoc.id))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kullanıcı bulunamadı")
        }
    }
    
    // 6. Kullanıcı rolünü güncelle (Admin paneli için)
    override suspend fun updateUserRole(uid: String, newRole: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update("role", newRole)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Rol güncellenemedi.")
        }
    }
}