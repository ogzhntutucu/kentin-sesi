package io.github.thwisse.kentinsesi.data.model

data class User(
    val uid: String = "",
    val email: String = "",

    // --- YENİ EKLENEN ALANLAR ---
    val fullName: String = "", // Artık kayıt anında değil, profil tamamlama ekranında dolacak
    val username: String = "", // @kullaniciadi gibi (isteğe bağlı)
    val city: String = "",     // Örn: Hatay
    val district: String = "", // Örn: İskenderun
    // ---------------------------

    val role: String = "citizen",
    val points: Long = 0,
    val title: String = "Yeni Kullanıcı"
) {
    // Firestore için boş constructor gerekliliği, varsayılan değerlerle ( = "" ) sağlanmış oldu.
}