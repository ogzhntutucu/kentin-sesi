package io.github.thwisse.kentinsesi.util

import io.github.thwisse.kentinsesi.data.model.User
import io.github.thwisse.kentinsesi.data.model.UserRole

/**
 * Yetkilendirme (Authorization) yardımcı fonksiyonları
 * 
 * Bu dosya, kullanıcı rollerine göre yetki kontrolü yapmak için kullanılır.
 */
object AuthorizationUtils {
    
    /**
     * Kullanıcının yetkili (official) olup olmadığını kontrol eder
     * @param user Kontrol edilecek kullanıcı
     * @return true eğer kullanıcı official veya admin ise
     */
    fun isOfficial(user: User?): Boolean {
        return user?.isOfficial == true
    }
    
    /**
     * Kullanıcının admin olup olmadığını kontrol eder
     * @param user Kontrol edilecek kullanıcı
     * @return true eğer kullanıcı admin ise
     */
    fun isAdmin(user: User?): Boolean {
        return user?.isAdmin == true
    }
    
    /**
     * Kullanıcının vatandaş olup olmadığını kontrol eder
     * @param user Kontrol edilecek kullanıcı
     * @return true eğer kullanıcı citizen ise
     */
    fun isCitizen(user: User?): Boolean {
        return user?.roleEnum == UserRole.CITIZEN
    }
    
    /**
     * Kullanıcının post durumunu güncelleyip güncelleyemeyeceğini kontrol eder
     * Sadece yetkili (official/admin) kullanıcılar post durumunu güncelleyebilir
     * @param user Kontrol edilecek kullanıcı
     * @return true eğer kullanıcı post durumunu güncelleyebilirse
     */
    fun canUpdatePostStatus(user: User?): Boolean {
        return isOfficial(user)
    }
    
    /**
     * Kullanıcının post silebilip silemeyeceğini kontrol eder
     * - Post sahibi kendi postunu silebilir
     * - Admin her postu silebilir
     * @param user Kontrol edilecek kullanıcı
     * @param postAuthorId Post'un sahibinin ID'si
     * @return true eğer kullanıcı postu silebilirse
     */
    fun canDeletePost(user: User?, postAuthorId: String?): Boolean {
        if (user == null || postAuthorId == null) return false
        return user.uid == postAuthorId || isAdmin(user)
    }
    
    /**
     * Kullanıcının post oluşturup oluşturamayacağını kontrol eder
     * Şu an için tüm giriş yapmış kullanıcılar post oluşturabilir
     * @param user Kontrol edilecek kullanıcı
     * @return true eğer kullanıcı post oluşturabilirse
     */
    fun canCreatePost(user: User?): Boolean {
        return user != null && user.uid.isNotEmpty()
    }
}

