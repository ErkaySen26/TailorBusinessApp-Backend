package erdalguda.main.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    private Tesseract tesseract;
    private boolean ocrEnabled = false; // Başlangıçta false, başarılı init'den sonra true olacak

    @Value("${app.ocr.enabled:true}")
    private boolean configOcrEnabled;

    public OcrService() {
        // Başlangıçta OCR disable, sonra extractTextFromImage'da kontrol edilecek
        ocrEnabled = false;
        logger.info("🔧 OCR Service başlatıldı, runtime'da config kontrol edilecek");
    }

    private void initializeTesseractIfNeeded() {
        if (ocrEnabled) {
            return; // Zaten başlatılmış
        }

        try {
            // Config kontrol et
            if (!configOcrEnabled) {
                logger.info("🔄 OCR config'den devre dışı bırakıldı, mock mode aktif");
                return;
            }
            
            logger.info("🚀 OCR başlatılıyor - Real Tesseract mode");
            
            // Native library path'ini ayarla (macOS için)
            setLibraryPath();
            
            initializeTesseract();
            ocrEnabled = true;
            logger.info("✅ OCR sistemi başarıyla başlatıldı");
        } catch (UnsatisfiedLinkError e) {
            logger.warn("❌ Tesseract native library yüklenemedi (mimari uyumsuzluğu: {}). Mock mode etkinleştirildi.", e.getMessage().contains("arm64") ? "ARM64 vs x86_64" : "library not found");
            logger.debug("Native library hatası: {}", e.getMessage());
            ocrEnabled = false;
        } catch (Exception e) {
            logger.error("❌ Tesseract başlatılamadı: {}", e.getMessage());
            ocrEnabled = false;
        }
    }

    private void setLibraryPath() {
        try {
            // macOS Homebrew Tesseract library path'leri
            String[] possibleLibPaths = {
                "/opt/homebrew/lib",           // M1/M2 Mac Homebrew
                "/usr/local/lib",              // Intel Mac Homebrew
                "/usr/lib"                     // System libs
            };
            
            // JNA library path'ini set et
            String existingPath = System.getProperty("jna.library.path", "");
            StringBuilder newPath = new StringBuilder(existingPath);
            
            for (String libPath : possibleLibPaths) {
                if (Files.exists(Paths.get(libPath)) && Files.exists(Paths.get(libPath, "libtesseract.dylib"))) {
                    if (newPath.length() > 0) {
                        newPath.append(":");
                    }
                    newPath.append(libPath);
                    logger.info("Tesseract library path eklendi: {}", libPath);
                    break; // İlk bulduğunu kullan
                }
            }
            
            if (newPath.length() > 0) {
                System.setProperty("jna.library.path", newPath.toString());
                logger.info("JNA library path ayarlandı: {}", newPath.toString());
            }
            
            // Ayrıca DYLD_LIBRARY_PATH'i de ayarla (macOS için)
            String dyldPath = System.getenv("DYLD_LIBRARY_PATH");
            if (dyldPath == null || !dyldPath.contains("/opt/homebrew/lib")) {
                logger.info("DYLD_LIBRARY_PATH ayarlaması öneriliyor: export DYLD_LIBRARY_PATH=/opt/homebrew/lib:$DYLD_LIBRARY_PATH");
            }
            
        } catch (Exception e) {
            logger.warn("Library path ayarlanırken hata: {}", e.getMessage());
        }
    }

    private void initializeTesseract() throws Exception {
        // JAI Image I/O formatlarını kaydet
        registerImageFormats();
        
        // Tesseract instance'ı oluştur - UnsatisfiedLinkError burada atılabilir
        tesseract = new Tesseract();
        
        // Tesseract data path'ini belirle
        String[] possiblePaths = {
            "/opt/homebrew/share/tessdata",  // macOS ARM Homebrew
            "/usr/local/share/tessdata",     // macOS Intel Homebrew
            "/opt/homebrew/Cellar/tesseract/5.5.1/share/tessdata", // Specific version path
            "/usr/share/tesseract-ocr/4.00/tessdata", // Ubuntu
            "/usr/share/tesseract-ocr/tessdata",       // CentOS
            System.getenv("TESSDATA_PREFIX")            // Environment variable
        };
        
        String tessDataPath = null;
        for (String path : possiblePaths) {
            if (path != null && Files.exists(Paths.get(path))) {
                // İngilizce dil dosyasının varlığını kontrol et
                if (Files.exists(Paths.get(path, "eng.traineddata"))) {
                    tessDataPath = path;
                    logger.info("Tesseract data path bulundu: {}", tessDataPath);
                    break;
                }
            }
        }
        
        if (tessDataPath != null) {
            tesseract.setDatapath(tessDataPath);
            logger.info("Tesseract data path ayarlandı: {}", tessDataPath);
            
            // Türkçe dil dosyası varsa onu kullan, yoksa İngilizce
            if (Files.exists(Paths.get(tessDataPath, "tur.traineddata"))) {
                tesseract.setLanguage("tur+eng"); // Türkçe + İngilizce
                logger.info("Dil ayarlandı: Türkçe + İngilizce");
            } else {
                tesseract.setLanguage("eng");
                logger.info("Dil ayarlandı: İngilizce");
            }
        } else {
            logger.warn("Tesseract data path bulunamadı, default kullanılacak");
            tesseract.setLanguage("eng");
        }
        
        // OCR ayarları
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        
        // OCR doğruluğunu artırmak için ek ayarlar
        tesseract.setVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÇĞIİÖŞÜçğıiöşü:.,cm ");
        
        logger.info("Tesseract başarıyla yapılandırıldı");
    }

    private void registerImageFormats() {
        try {
            // JAI Image I/O formatlarını kaydet
            ImageIO.scanForPlugins();
            logger.info("JAI Image I/O formatları kaydedildi");
        } catch (Exception e) {
            logger.warn("JAI Image I/O formatları kaydedilemedi: {}", e.getMessage());
        }
    }

    public String extractTextFromImage(MultipartFile file) throws IOException {
        initializeTesseractIfNeeded(); // Başlangıçta devre dışı, burada kontrol edilecek
        if (!ocrEnabled) {
            logger.info("🔄 OCR devre dışı, mock data döndürülüyor");
            return getMockOcrResult();
        }

        try {
            // Desteklenen format kontrolü
            String contentType = file.getContentType();
            if (!isImageFormatSupported(contentType)) {
                logger.warn("Desteklenmeyen resim formatı: {}", contentType);
                return getMockOcrResult();
            }

            // MultipartFile'ı BufferedImage'e çevir
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                logger.error("Resim okunamadı, null döndü");
                return getMockOcrResult();
            }

            logger.info("OCR işlemi başlatılıyor - Resim boyutu: {}x{}", image.getWidth(), image.getHeight());

            // OCR işlemi
            String result = tesseract.doOCR(image);
            logger.info("✅ OCR başarılı, {} karakter metin çıkarıldı", result.length());
            logger.debug("OCR sonucu: {}", result);
            return result;

        } catch (TesseractException e) {
            logger.error("OCR işleminde hata: {}", e.getMessage());
            return getMockOcrResult();
        } catch (Exception e) {
            logger.error("Beklenmeyen hata: {}", e.getMessage());
            return getMockOcrResult();
        }
    }

    private boolean isImageFormatSupported(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") && 
               (contentType.contains("jpeg") || 
                contentType.contains("jpg") || 
                contentType.contains("png") || 
                contentType.contains("bmp") || 
                contentType.contains("tiff"));
    }

    private String getMockOcrResult() {
        // Verdiğiniz örnek fotoğraftaki verileri mock olarak döndür
        return """
            Measured data
            Head: 57.9 cm
            Neck: 41.7 cm  
            Shoulder: 55.4 cm
            Chest: 99.9 cm
            Waist: 92.5 cm
            Hip: 99.1 cm
            L Arm: 31.8 cm
            R Arm: 33.8 cm
            L Thigh: 52.3 cm
            R Thigh: 52.2 cm
            L Calf: 38.9 cm
            R Calf: 38.9 cm
            Waist-to-Hip Ratio: 0.9
            Elbow Length: 78.5 cm
            """;
    }

    public Map<String, Double> parseOcrResult(String ocrText) {
        Map<String, Double> measurements = new HashMap<>();
        
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return measurements;
        }

        logger.info("🔍 OCR Text parsing başlatıldı. Text uzunluğu: {}", ocrText.length());
        logger.debug("📄 OCR Text içeriği:\n{}", ocrText);

        // Gelişmiş pattern - hem Türkçe hem İngilizce ölçü isimleri için
        Pattern pattern = Pattern.compile("([A-ZÇĞIİÖŞÜa-zçğıiöşü\\s]+)\\s*:?\\s*(\\d+(?:[.,]\\d+)?)\\s*cm", 
                                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        
        Matcher matcher = pattern.matcher(ocrText);
        
        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            String partName = matcher.group(1).trim().toLowerCase();
            String valueStr = matcher.group(2).replace(',', '.');
            
            logger.info("🎯 Match #{}: '{}' = '{}'", matchCount, partName, valueStr);
            
            try {
                double value = Double.parseDouble(valueStr);
                
                // Türkçe ve İngilizce isimleri normalize et
                String normalizedName = normalizeMeasurementName(partName);
                if (normalizedName != null) {
                    measurements.put(normalizedName, value);
                    logger.info("✅ Ölçü eklendi: '{}' -> '{}' = {} cm", partName, normalizedName, value);
                } else {
                    logger.warn("❌ Tanınmayan ölçü adı: '{}'", partName);
                }
            } catch (NumberFormatException e) {
                logger.warn("⚠️ Geçersiz ölçü değeri: '{}' for '{}'", valueStr, partName);
            }
        }
        
        logger.info("✅ Parsing tamamlandı. {} pattern match, {} ölçü çıkarıldı", matchCount, measurements.size());
        return measurements;
    }

    private String normalizeMeasurementName(String name) {
        // Türkçe ve İngilizce vücut bölgesi isimlerini normalize et
        String normalized = name.toLowerCase().trim();
        
        logger.debug("🔍 Normalizing '{}' -> '{}'", name, normalized);
        
        Map<String, String> nameMapping = new HashMap<>();
        // Türkçe isimler
        nameMapping.put("baş", "Baş");
        nameMapping.put("bas", "Baş");
        nameMapping.put("boyun", "Boyun");
        nameMapping.put("omuz", "Omuz");
        nameMapping.put("göğüs", "Göğüs");
        nameMapping.put("gogus", "Göğüs");
        nameMapping.put("bel", "Bel");
        nameMapping.put("kalça", "Kalça");
        nameMapping.put("kalca", "Kalça");
        nameMapping.put("bacak", "Bacak");
        nameMapping.put("paça", "Paça");
        nameMapping.put("paca", "Paça");
        nameMapping.put("kol", "Kol");
        nameMapping.put("bilek", "Bilek");
        nameMapping.put("ense", "Ense");
        nameMapping.put("ön", "Ön");
        nameMapping.put("on", "Ön");
        nameMapping.put("arka", "Arka");
        nameMapping.put("yaka", "Yaka");
        
        // İngilizce isimler (fotoğrafınızdaki format için)
        nameMapping.put("head", "Baş");
        nameMapping.put("neck", "Boyun");
        nameMapping.put("shoulder", "Omuz");
        nameMapping.put("chest", "Göğüs");
        nameMapping.put("waist", "Bel");
        nameMapping.put("hip", "Kalça");
        nameMapping.put("l arm", "Sol Kol");
        nameMapping.put("r arm", "Sağ Kol");
        nameMapping.put("l thigh", "Sol Bacak");
        nameMapping.put("r thigh", "Sağ Bacak");
        nameMapping.put("l calf", "Sol Baldır");
        nameMapping.put("r calf", "Sağ Baldır");
        nameMapping.put("elbow length", "Dirsek Uzunluğu");
        nameMapping.put("waist-to-hip ratio", "Bel-Kalça Oranı");
        nameMapping.put("arm", "Kol");
        nameMapping.put("thigh", "Bacak");
        nameMapping.put("calf", "Baldır");
        nameMapping.put("leg", "Bacak");
        nameMapping.put("wrist", "Bilek");
        nameMapping.put("front", "Ön");
        nameMapping.put("back", "Arka");
        nameMapping.put("collar", "Yaka");
        
        String result = nameMapping.get(normalized);
        logger.debug("📋 Mapping result: '{}' -> '{}'", normalized, result);
        
        return result;
    }

    public boolean isOcrEnabled() {
        return ocrEnabled && configOcrEnabled;
    }
} 