# TailorBusinessApp

<p align="center">
  <img src="https://raw.githubusercontent.com/kullaniciadi/TailorBusinessApp/main/assets/logo.png" width="120" alt="TailorBusinessApp Logo"/>
</p>

<p align="center">
  <b>Terzi işletmeleri için müşteri, sipariş, blog ve kategori yönetimi sağlayan modern, güvenli ve ölçeklenebilir bir Spring Boot uygulaması.</b>
</p>

---

## İçindekiler
- [Tanıtım](#tanıtım)
- [Proje Mimarisi ve Dosya Yapısı](#proje-mimarisi-ve-dosya-yapısı)
- [Özellikler](#özellikler)
- [Ekran Görüntüleri](#ekran-görüntüleri)
- [Kurulum](#kurulum)
- [Yapılandırma](#yapılandırma)
- [Kullanım](#kullanım)
- [API Endpointleri](#api-endpointleri)
- [Katkıda Bulunma](#katkıda-bulunma)
- [Lisans](#lisans)
- [İletişim](#iletişim)

---

## Tanıtım
TailorBusinessApp, terzi işletmelerinin dijitalleşmesini kolaylaştırmak için geliştirilmiş, müşteri ve sipariş yönetimi, blog ve kategori yönetimi, raporlama ve dosya yükleme gibi işlevleri bir arada sunan kapsamlı bir web uygulamasıdır. Modern yazılım standartlarına uygun olarak geliştirilmiş olup, güvenlik ve ölçeklenebilirlik ön planda tutulmuştur.

---

## Proje Mimarisi ve Dosya Yapısı

```
TailorBusinessApp/
├── src/
│   ├── main/
│   │   ├── java/erdalguda/main/
│   │   │   ├── config/         # Uygulama ve güvenlik konfigürasyonları
│   │   │   ├── controller/     # REST API controller'ları
│   │   │   ├── dto/            # Veri transfer objeleri
│   │   │   ├── model/          # JPA entity'leri
│   │   │   ├── repository/     # Spring Data JPA repository'leri
│   │   │   ├── security/       # JWT ve güvenlik bileşenleri
│   │   │   ├── service/        # İş mantığı katmanı
│   │   │   └── util/           # Yardımcı sınıflar
│   │   └── resources/
│   │       ├── application.properties # Konfigürasyon dosyası
│   │       ├── db-schema.sql         # Veritabanı şeması
│   │       └── db-indexes.sql        # Veritabanı indexleri
│   └── test/
│       └── java/erdalguda/main/      # Testler
├── pom.xml                           # Maven yapılandırması
├── README.md                         # Proje dokümantasyonu
└── ...
```

### Katmanlar
- **Controller:** API uç noktalarını yönetir.
- **Service:** İş mantığı ve servis katmanı.
- **Repository:** Veritabanı işlemleri.
- **Model:** Entity ve domain nesneleri.
- **DTO:** Veri transferi için kullanılır.
- **Config/Security:** Uygulama ve güvenlik ayarları.

---

## Özellikler
- Müşteri, sipariş, blog ve kategori yönetimi
- JWT tabanlı kimlik doğrulama
- AWS S3 ile dosya yükleme
- Raporlama ve istatistikler
- Gelişmiş hata yönetimi ve loglama
- Modern RESTful API mimarisi
- Kolay özelleştirilebilir yapı

---

## Ekran Görüntüleri

<p align="center">
  <img src="https://raw.githubusercontent.com/kullaniciadi/TailorBusinessApp/main/assets/dashboard.png" width="600" alt="Dashboard"/>
  <br/>
  <i>Yönetim Paneli</i>
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/kullaniciadi/TailorBusinessApp/main/assets/customers.png" width="600" alt="Müşteri Listesi"/>
  <br/>
  <i>Müşteri Listesi</i>
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/kullaniciadi/TailorBusinessApp/main/assets/order-detail.png" width="600" alt="Sipariş Detayı"/>
  <br/>
  <i>Sipariş Detayı</i>
</p>

---

## Kurulum
1. **Java 17+** ve **Maven** yüklü olmalıdır.
2. Projeyi klonlayın:
   ```bash
   git clone https://github.com/kullaniciadi/TailorBusinessApp.git
   cd TailorBusinessApp
   ```
3. Bağımlılıkları yükleyin ve projeyi derleyin:
   ```bash
   ./mvnw clean install
   ```
4. Uygulamayı başlatın:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Varsayılan olarak uygulama `http://localhost:8080` adresinde çalışır.

---

## Yapılandırma
`src/main/resources/application.properties` dosyasındaki ayarları kendi ortamınıza göre düzenleyin:
- Veritabanı bağlantı bilgileri
- AWS S3 erişim anahtarları
- JWT gizli anahtarı

---

## Kullanım
API endpointlerine Postman veya benzeri araçlarla istek gönderebilirsiniz. JWT ile korunan endpointler için öncelikle giriş yapıp token almalısınız.

### Giriş (Login)
```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "sifre"
}
```
Başarılı giriş sonrası dönen JWT token ile korumalı endpointlere erişebilirsiniz.

---

## API Endpointleri

| Endpoint                | Metot | Açıklama                |
|-------------------------|-------|-------------------------|
| /api/auth/login         | POST  | Giriş yap, JWT al       |
| /api/customer           | GET   | Müşterileri listele     |
| /api/customer           | POST  | Yeni müşteri ekle       |
| /api/order              | GET   | Siparişleri listele     |
| /api/order              | POST  | Sipariş oluştur         |
| /api/blog               | GET   | Blogları listele        |
| /api/blog               | POST  | Blog ekle               |
| /api/category           | GET   | Kategorileri listele    |
| /api/report/orders      | GET   | Sipariş raporu al       |
| /api/upload             | POST  | Dosya yükle (AWS S3)    |

#### Örnek: Yeni Müşteri Ekle
```http
POST /api/customer
Content-Type: application/json
{
  "name": "Ali Veli",
  "phone": "5551234567"
}
```

#### Örnek: Sipariş Raporu Al
```http
GET /api/report/orders
Authorization: Bearer <token>
```

#### Örnek: Dosya Yükle
```http
POST /api/upload
Authorization: Bearer <token>
Form-Data: file=<dosya>
```

---

## Katkıda Bulunma
Katkılarınızı memnuniyetle karşılıyoruz! Lütfen önce bir issue açın, ardından pull request gönderin. Kod standartlarına ve proje mimarisine uygun katkılar beklenmektedir.

---

## Lisans
Bu proje MIT lisansı ile lisanslanmıştır.

---

## İletişim
Her türlü soru ve öneriniz için: [erdalguda@example.com](mailto:erdalguda@example.com)

---
Daha fazla bilgi için kodu inceleyebilir veya doğrudan iletişime geçebilirsiniz.
