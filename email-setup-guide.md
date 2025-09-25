# Email Sistemi Kurulum Rehberi

## 1. Gmail App Password Oluşturma

1. Gmail hesabınıza giriş yapın
2. Google Account Settings → Security → 2-Step Verification aktif olmalı
3. App Passwords'a gidin
4. "Mail" için yeni bir app password oluşturun
5. Oluşturulan 16 haneli şifreyi kopyalayın

## 2. Environment Variables Ayarlama

### Mac/Linux için terminal'de:
```bash
export MAIL_USERNAME="your-gmail@gmail.com"
export MAIL_PASSWORD="your-16-digit-app-password"
```

### Windows için:
```cmd
set MAIL_USERNAME=your-gmail@gmail.com
set MAIL_PASSWORD=your-16-digit-app-password
```

### IDE'de (IntelliJ/Eclipse) Environment Variables:
```
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-16-digit-app-password
```

## 3. Test Etme

1. Spring Boot'u yeniden başlatın
2. POST isteği gönderin: `http://localhost:6767/api/test/email`
   ```json
   {
     "email": "test@example.com"
   }
   ```

## 4. Alternatif Email Providers

### Outlook/Hotmail:
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
```

### Yahoo:
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
```

## 5. Debug

Console'da şu log'ları arayın:
- "Test email gönderiliyor: ..."
- "Sipariş durum güncelleme emaili gönderildi..."
- "Email gönderilirken hata oluştu..." 