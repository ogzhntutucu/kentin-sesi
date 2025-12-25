# Firestore Security Rules Kurulumu

Bu dosya, Firebase Firestore güvenlik kurallarını içerir. Bu kurallar, veritabanına erişimi kontrol eder.

## Kurulum Adımları

1. **Firebase Console'a Git**
   - https://console.firebase.google.com adresine git
   - Projeni seç

2. **Firestore Database'e Git**
   - Sol menüden "Firestore Database" seç
   - "Rules" sekmesine tıkla

3. **Kuralları Yapıştır**
   - `firestore.rules` dosyasının içeriğini kopyala
   - Firebase Console'daki editöre yapıştır
   - "Publish" butonuna tıkla

## Kuralların Açıklaması

### Users Collection
- ✅ **Okuma**: Giriş yapmış herkes okuyabilir
- ✅ **Oluşturma**: Sadece kendi profilini oluşturabilir
- ✅ **Güncelleme**: Sadece kendi profilini güncelleyebilir (admin hariç)
- ✅ **Silme**: Sadece admin silebilir

### Posts Collection
- ✅ **Okuma**: Giriş yapmış herkes okuyabilir
- ✅ **Oluşturma**: Giriş yapmış herkes post oluşturabilir
- ✅ **Güncelleme**: 
  - Post sahibi: title, description, category, imageUrl güncelleyebilir
  - Yetkili kullanıcılar: status güncelleyebilir
- ✅ **Silme**: Post sahibi veya admin silebilir

### Comments Subcollection
- ✅ **Okuma**: Giriş yapmış herkes okuyabilir
- ✅ **Oluşturma**: Giriş yapmış herkes yorum yapabilir
- ✅ **Güncelleme**: Sadece yorum sahibi güncelleyebilir
- ✅ **Silme**: Yorum sahibi veya admin silebilir

## Önemli Notlar

⚠️ **Bu kuralları yayınlamadan önce test et!**

Firebase Console'da "Rules Playground" kullanarak kuralları test edebilirsin.

## Test Senaryoları

1. ✅ Normal kullanıcı kendi postunu oluşturabilir
2. ✅ Normal kullanıcı başkasının postunu okuyabilir
3. ❌ Normal kullanıcı başkasının postunu silemez
4. ✅ Yetkili kullanıcı post status'unu güncelleyebilir
5. ❌ Normal kullanıcı post status'unu güncelleyemez

