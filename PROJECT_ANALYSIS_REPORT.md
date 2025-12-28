 # Kentin Sesi — Proje Analiz Raporu (Güncel)

 **Tarih:** 2025-12-28  
 **Kapsam:** Kod tabanı + Gradle + Firestore rules + UI katmanı  
 **Hedef:** Projeyi sektör standartlarında, sürdürülebilir ve production-ready bir yapıya yaklaştırmak

 ---

 ## 1) Kısa Özet (Executive Summary)

 Proje, MVVM + Repository + Hilt temelleri açısından **doğru yolda**. Firestore kuralları var ve yakın zamanda şema temizliği yapılmış.

 Ancak şu anda production standardı için en büyük riskler:

 - **[Performans/Maliyet]** Görsel yükleme tarafında **sıkıştırma yok** (`putFile(imageUri)` direkt).  
 - **[Maliyet/Bakım]** Post silmede **Storage temizliği yok** (orphan file riski).  
 - **[Release Readiness]** **R8/ProGuard kapalı** (`isMinifyEnabled=false`) ve `proguard-rules.pro` template.  
 - **[Ölçeklenebilirlik]** `getPosts()` çoğu senaryoda **tam liste çekip client-side filtreliyor** (pagination yok).  
 - **[DX/Observability]** Logging standardı yok, çok yerde `Log.*` var.  
 - **[Kalite]** Test coverage pratikte yok.

 ---

 ## 2) Doğrulama: Eski Rapordaki Maddeler Ne Kadar Güncel?

 ### 2.1 Artık Geçersiz (Çözülmüş / Güncellenmiş)

 - **[Comment DiffUtil sorunu]** Artık geçersiz.
   - **Kanıt:** `CommentAdapter.CommentDiffCallback.areItemsTheSame()` artık `oldItem.id == newItem.id` kullanıyor.

 - **[Post ID tutarsızlığı (id vs postId)]** Büyük ölçüde çözülmüş.
   - **Kanıt:** `Post` modelinde kanonik alan `@DocumentId val id` ve repository tarafında boş gelirse `doc.id` fallback mevcut.

 ### 2.2 Hala Geçerli (Bugün de doğru)

 - **[Image compression yok]** Geçerli.
   - **Kanıt:** `PostRepositoryImpl.createPost()` -> `storageRef.putFile(imageUri).await()`.
   - **Not:** `Constants.MAX_IMAGE_SIZE_MB` ve `Constants.IMAGE_COMPRESSION_QUALITY` tanımlı ama kullanılmıyor.

 - **[Storage cleanup yok]** Geçerli.
   - **Kanıt:** `PostRepositoryImpl.deletePost()` sadece Firestore delete, Storage için TODO yorumları var.

 - **[Pagination yok]** Geçerli.
   - **Kanıt:** `getPosts()` birçok dalda `.get().await()` ile tüm sonuçları çekiyor.
   - **Not:** `Constants.POSTS_PAGE_SIZE` tanımlı ama kullanılmuyor.

 - **[R8/ProGuard rules eksik / minify kapalı]** Geçerli.
   - **Kanıt:** `app/build.gradle.kts` -> `release { isMinifyEnabled = false }`.
   - **Kanıt:** `app/proguard-rules.pro` template.

 - **[Hardcoded strings]** Geçerli.
   - **Kanıt:** `PostAdapter` durum label’ları (`"new" -> "Yeni"` vb.).

 - **[Logging standardı yok]** Geçerli.
   - **Kanıt:** Projede birden fazla yerde `Log.*` kullanımı mevcut.

 - **[Genel Exception yakalama]** Geçerli.
   - **Kanıt:** Repository’lerde `catch (e: Exception)` + `Resource.Error(e.message ?: ...)` paterni yaygın.

 - **[Offline persistence / caching]** Geçerli.
   - **Kanıt:** Firestore settings / offline persistence enable eden bir konfig bulunamadı.

 ---

 ## 3) Mevcut Mimari Fotoğrafı (As-Is)

 - **UI:** Fragment tabanlı + ViewBinding + Navigation Component.
 - **State:** ViewModel’ler + `Resource<T>` wrapper + LiveData/Flow karışık kullanım.
 - **DI:** Hilt (Firebase ve repository binding’leri modüllerde).
 - **Data sources:**
   - Firestore (posts/users/comments/usernames)
   - Firebase Storage (post görselleri)
   - Room (filter preset)
   - DataStore (son filtre kriterleri / preset seçimi)

 ---

 ## 4) Bulgular ve Öneriler (Öncelikli)

 Aşağıdaki maddelerde format standardı:

 - **[Durum]** Mevcut mu?
 - **[Etki]** Neyi bozuyor / risk?
 - **[Öneri]** Ne yapılmalı?
 - **[Efor]** S / M / L (tahmini)

 ### 4.1 Performans / Maliyet (Yüksek Öncelik)

 1) **Görsel sıkıştırma/yeniden boyutlandırma yok**
 - **Durum:** Mevcut.
 - **Etki:** Upload süresi + Storage maliyeti + ağ kullanımı artar.
 - **Öneri:**
   - `Uri` -> bitmap decode + downscale + JPEG compress (`IMAGE_COMPRESSION_QUALITY`)
   - 5MB üstü dosyaları reddet veya yeniden boyutlandır.
 - **Efor:** M

 2) **Post silmede Storage dosyası temizlenmiyor (orphan file)**
 - **Durum:** Mevcut.
 - **Etki:** Storage maliyeti kontrolsüz büyür; veri hijyeni bozulur.
 - **Öneri:**
   - Post dokümanına `imageStoragePath` (veya fileName) yaz.
   - Delete akışında önce Storage delete (best-effort) + sonra Firestore delete (veya tersi; hata senaryolarını tasarla).
 - **Efor:** M

 3) **Posts listeleme ölçeklenmiyor (pagination yok + client-side filtreleme)**
 - **Durum:** Mevcut.
 - **Etki:** Read cost ve RAM artar; büyük veri setinde UI yavaşlar.
 - **Öneri:**
   - Basit: tarih sıralı `limit(POSTS_PAGE_SIZE)` + `startAfter(lastDoc)` pagination.
   - Filtre stratejisi: tek `whereIn` limitine takılmamak için “query modeli” netleştir (ya server-side index ile kısıtla ya da filtreleri yeniden tasarla).
 - **Efor:** L

 ### 4.2 Release Readiness (Yüksek Öncelik)

 4) **Release build’te minify kapalı + ProGuard rules boş**
 - **Durum:** Mevcut (`isMinifyEnabled=false`).
 - **Etki:** APK boyutu büyür; reverse engineering daha kolay; bazı optimizasyonlar kaçırılır.
 - **Öneri:**
   - Önce staging/release variant’ı ayır, minify’ı kademeli aç.
   - Firebase/Hilt/Room için gerekli keep rules ekle (dokümantasyondan).
 - **Efor:** M

 ### 4.3 Hata Yönetimi / Kullanıcı Deneyimi (Orta-Yüksek)

 5) **Genel `Exception` yakalama + kullanıcıya ham mesaj**
 - **Durum:** Mevcut.
 - **Etki:** UX zayıf; debug zor; hatalar sınıflandırılamıyor.
 - **Öneri:**
   - Domain seviyesinde hata modeli: `sealed class AppError` (Network/Auth/Permission/Validation/Unknown)
   - Firebase exception’larını map et (permission-denied vb.).
 - **Efor:** M

 6) **Network/offline durumu yönetimi zayıf**
 - **Durum:** Mevcut.
 - **Etki:** Offline senaryoda belirsiz hata; retry/queue davranışı yok.
 - **Öneri:**
   - Basit: connectivity check + kullanıcıya “offline” state.
   - Gelişmiş: Firestore offline persistence + UI’da sync/queue göstergesi.
 - **Efor:** S→M

 ### 4.4 Kod Kalitesi / Sürdürülebilirlik (Orta)

 7) **Hardcoded UI string’ler**
 - **Durum:** Mevcut (`PostAdapter` status label).
 - **Etki:** Lokalizasyon zor; tek noktadan yönetim yok.
 - **Öneri:** `strings.xml` + mapping (örn. `PostStatus` -> stringRes).
 - **Efor:** S

 8) **Stale TODO (RepositoryModule)**
 - **Durum:** Mevcut.
 - **Etki:** Yanıltıcı dokümantasyon; bakım maliyeti.
 - **Öneri:** Eski TODO’yu kaldır.
 - **Efor:** S

 9) **Logging standardı yok**
 - **Durum:** Mevcut (`Log.*`).
 - **Etki:** Üretimde gürültü; takip edilebilirlik düşük.
 - **Öneri:** Timber veya küçük bir `Logger` wrapper + buildType bazlı davranış.
 - **Efor:** S→M

 ### 4.5 Test / Güven (Yüksek)

 10) **Test coverage pratikte yok**
 - **Durum:** Mevcut.
 - **Etki:** Refactor maliyeti artar; regresyon riski.
 - **Öneri (minimum viable test):**
   - `ValidationUtils` unit test
   - ViewModel’ler için coroutine test + fake repo
   - Repository için en azından “mapping / input validation” testleri
 - **Efor:** L (kademeli başlatılabilir)

 ---

 ## 5) Ek Mimari Öneriler (İdeal Hedef)

 Eğer hedef “sektör standardı, temiz mimari” ise, aşağıdaki adımlar projeyi net biçimde iyileştirir:

 - **Domain katmanı ekle:** UseCase’ler (örn. `CreatePostUseCase`, `ToggleUpvoteUseCase`).
 - **UI State standardı:** Her ekranda `UiState` (Loading/Content/Error/Empty) + tek tip error model.
 - **Repository sınırları:** Repository yalnızca data erişimi + mapping; UI kararları (string format, label) repository’ye sızmasın.
 - **Constants/Canonical değerler:** Firestore’da saklanan kategori değerleri `Constants.CATEGORY_*` gibi canonical code olsun; UI label `strings.xml` ile çözülsün.

 ---

 ## 6) Önceliklendirilmiş Roadmap

 ### 0–2 hafta (En yüksek ROI)
 - Image compression + max size kontrolü
 - Storage cleanup (imageStoragePath + delete)
 - RepositoryModule içindeki stale TODO kaldırma
 - Hardcoded status string’lerini `strings.xml`’e taşıma
 - Logging wrapper (min seviyede)

 ### 2–6 hafta (Production hazırlığı)
 - Pagination + load-more/infinite scroll
 - Hata modeli + error UI standardı (Snackbar + retry)
 - R8/ProGuard: variant bazlı etkinleştirme + rules
 - Connectivity/offline state (en azından kullanıcıya net durum)

 ### 6+ hafta (Kurumsal kalite)
 - Test altyapısı (unit + viewmodel)
 - Static analysis (ktlint/detekt) + CI
 - Crashlytics/Performance Monitoring

 ---

 ## 7) Sonuç

 Proje **temel mimari açısından sağlıklı** ve geliştirmeye uygun. Şu anki en kritik eksikler, büyük ölçüde “production sertleştirme” başlığında toplanıyor: görsel optimizasyonu, storage hijyeni, ölçeklenebilir listeleme, release yapılandırması, test ve observability.

 Bu rapor “yol haritası” gibi kullanılabilir: önce maliyet/perf ve release readiness, sonra ölçek/test/ci.
