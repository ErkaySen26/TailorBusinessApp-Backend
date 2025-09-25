package erdalguda.main.service;

import erdalguda.main.model.Measurement;
import erdalguda.main.repository.MeasurementRepository;
import erdalguda.main.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MeasurementService {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);
    
    @Autowired
    private MeasurementRepository measurementRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OcrService ocrService;
    
    /**
     * OCR sonucunu işleyip Measurement listesi oluşturur
     */
    public List<Measurement> processOcrResultForCustomer(String ocrText, Long customerId) {
        List<Measurement> processedMeasurements = new ArrayList<>();
        
        // Müşteri var mı kontrol et
        if (!customerRepository.existsById(customerId)) {
            logger.error("Müşteri bulunamadı: {}", customerId);
            throw new RuntimeException("Müşteri bulunamadı: " + customerId);
        }
        
        // OCR sonucunu parse et
        Map<String, Double> parsedMeasurements = ocrService.parseOcrResult(ocrText);
        
        if (parsedMeasurements.isEmpty()) {
            logger.warn("OCR'dan hiç ölçü çıkarılamadı");
            return processedMeasurements;
        }
        
        logger.info("Müşteri {} için {} ölçü işleniyor", customerId, parsedMeasurements.size());
        
        // Her ölçüyü ayrı ayrı işle (update or insert)
        for (Map.Entry<String, Double> entry : parsedMeasurements.entrySet()) {
            String regionName = entry.getKey();
            Double value = entry.getValue();
            
            try {
                // Mevcut ölçü var mı kontrol et
                Optional<Measurement> existingMeasurement = 
                    measurementRepository.findByCustomerIdAndRegionName(customerId, regionName);
                
                Measurement measurement;
                if (existingMeasurement.isPresent()) {
                    // Mevcut ölçüyü güncelle
                    measurement = existingMeasurement.get();
                    measurement.setValue(value);
                    measurement = measurementRepository.save(measurement);
                    logger.info("Ölçü güncellendi: {} = {} cm", regionName, value);
                } else {
                    // Yeni ölçü oluştur
                    measurement = new Measurement(customerId, regionName, value, "cm");
                    measurement = measurementRepository.save(measurement);
                    logger.info("Yeni ölçü eklendi: {} = {} cm", regionName, value);
                }
                
                processedMeasurements.add(measurement);
                
            } catch (Exception e) {
                logger.error("Ölçü işlenirken hata: {} = {}, Hata: {}", regionName, value, e.getMessage());
                // Hata durumunda devam et
            }
        }
        
        logger.info("Toplam {} ölçü başarıyla işlendi", processedMeasurements.size());
        return processedMeasurements;
    }
    
    /**
     * Eski method - geriye dönük uyumluluk için (deprecated)
     */
    @Deprecated
    public List<Measurement> processOcrResult(String ocrText) {
        logger.warn("processOcrResult(String) methodu deprecated. processOcrResultForCustomer kullanın.");
        return new ArrayList<>();
    }
    
    /**
     * Müşterinin tüm ölçülerini siler
     */
    @Transactional
    public void deleteAllMeasurements(Long customerId) {
        measurementRepository.deleteByCustomerId(customerId);
        logger.info("Müşteri {} için tüm ölçüler silindi", customerId);
    }
    
    /**
     * Belirli bir ölçü bölgesini günceller veya oluşturur
     */
    @Transactional
    public Measurement createOrUpdateMeasurement(Long customerId, String regionName, Double value, String unit) {
        try {
            // Müşteri var mı kontrol et
            if (!customerRepository.existsById(customerId)) {
                throw new RuntimeException("Müşteri bulunamadı: " + customerId);
            }
            
            Optional<Measurement> existingMeasurement = 
                measurementRepository.findByCustomerIdAndRegionName(customerId, regionName);
            
            Measurement measurement;
            if (existingMeasurement.isPresent()) {
                measurement = existingMeasurement.get();
                measurement.setValue(value);
                measurement.setUnit(unit);
                measurement = measurementRepository.save(measurement);
                logger.info("Ölçü güncellendi: {} = {} {}", regionName, value, unit);
            } else {
                measurement = new Measurement(customerId, regionName, value, unit);
                measurement = measurementRepository.save(measurement);
                logger.info("Yeni ölçü oluşturuldu: {} = {} {}", regionName, value, unit);
            }
            
            return measurement;
        } catch (Exception e) {
            logger.error("Ölçü oluşturma/güncelleme hatası: {} - {}", regionName, e.getMessage());
            throw e; // Exception'ı tekrar fırlat ki üst seviyede yakalanabilsin
        }
    }
    
    /**
     * Müşterinin tüm ölçülerini getirir
     */
    public List<Measurement> getCustomerMeasurements(Long customerId) {
        return measurementRepository.findByCustomerIdOrderByRegionNameAsc(customerId);
    }
    
    /**
     * İstatistiksel bilgiler
     */
    public Map<String, Object> getMeasurementStats(Long customerId) {
        List<Measurement> measurements = getCustomerMeasurements(customerId);
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", measurements.size());
        stats.put("lastUpdated", 
            measurements.stream()
                .map(Measurement::getUpdatedAt)
                .max(java.time.LocalDateTime::compareTo)
                .orElse(null)
        );
        
        return stats;
    }
} 