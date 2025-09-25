package erdalguda.main.controller;

import erdalguda.main.dto.MeasurementRequest;
import erdalguda.main.dto.MeasurementResponse;
import erdalguda.main.model.Measurement;
import erdalguda.main.repository.MeasurementRepository;
import erdalguda.main.repository.CustomerRepository;
import erdalguda.main.service.OcrService;
import erdalguda.main.service.MeasurementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {
    
    @Autowired
    private MeasurementRepository measurementRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OcrService ocrService;

    @Autowired
    private MeasurementService measurementService;
    
    // Müşterinin tüm ölçülerini getir
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerMeasurements(@PathVariable Long customerId) {
        try {
            // Müşteri var mı kontrol et
            if (!customerRepository.existsById(customerId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Müşteri bulunamadı"));
            }
            
            List<Measurement> measurements = measurementRepository.findByCustomerIdOrderByRegionNameAsc(customerId);
            List<MeasurementResponse> measurementResponses = measurements.stream()
                .map(MeasurementResponse::new)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("measurements", measurementResponses);
            response.put("count", measurements.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ölçüler yüklenirken hata oluştu: " + e.getMessage()));
        }
    }
    
    // Yeni ölçü ekle
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> addMeasurement(
            @PathVariable Long customerId,
            @RequestBody MeasurementRequest request) {
        try {
            // Müşteri var mı kontrol et
            if (!customerRepository.existsById(customerId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Müşteri bulunamadı"));
            }
            
            // Aynı bölge adı var mı kontrol et
            if (measurementRepository.findByCustomerIdAndRegionName(customerId, request.getRegionName()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bu müşteri için '" + request.getRegionName() + "' bölgesi zaten mevcut"));
            }
            
            Measurement measurement = new Measurement(
                customerId,
                request.getRegionName(),
                request.getValue(),
                request.getUnit()
            );
            
            Measurement savedMeasurement = measurementRepository.save(measurement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ölçü başarıyla eklendi");
            response.put("measurement", new MeasurementResponse(savedMeasurement));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ölçü eklenirken hata oluştu: " + e.getMessage()));
        }
    }
    
    // Ölçü güncelle
    @PutMapping("/{measurementId}")
    public ResponseEntity<Map<String, Object>> updateMeasurement(
            @PathVariable Long measurementId,
            @RequestBody MeasurementRequest request) {
        try {
            Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new RuntimeException("Ölçü bulunamadı"));
            
            // Bölge adı değiştiriliyorsa, aynı müşteride aynı bölge adı var mı kontrol et
            if (!measurement.getRegionName().equals(request.getRegionName())) {
                if (measurementRepository.findByCustomerIdAndRegionName(measurement.getCustomerId(), request.getRegionName()).isPresent()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bu müşteri için '" + request.getRegionName() + "' bölgesi zaten mevcut"));
                }
            }
            
            measurement.setRegionName(request.getRegionName());
            measurement.setValue(request.getValue());
            measurement.setUnit(request.getUnit());
            
            Measurement savedMeasurement = measurementRepository.save(measurement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ölçü başarıyla güncellendi");
            response.put("measurement", new MeasurementResponse(savedMeasurement));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ölçü güncellenirken hata oluştu: " + e.getMessage()));
        }
    }
    
    // Ölçü sil
    @DeleteMapping("/{measurementId}")
    public ResponseEntity<Map<String, Object>> deleteMeasurement(@PathVariable Long measurementId) {
        try {
            if (!measurementRepository.existsById(measurementId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ölçü bulunamadı"));
            }
            
            measurementRepository.deleteById(measurementId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ölçü başarıyla silindi"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ölçü silinirken hata oluştu: " + e.getMessage()));
        }
    }
    
    // Müşterinin tüm ölçülerini sil
    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> deleteAllCustomerMeasurements(@PathVariable Long customerId) {
        try {
            if (!customerRepository.existsById(customerId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Müşteri bulunamadı"));
            }
            
            measurementRepository.deleteByCustomerId(customerId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Müşterinin tüm ölçüleri silindi"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ölçüler silinirken hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-measurements/{customerId}")
    public ResponseEntity<Map<String, Object>> uploadMeasurements(
            @PathVariable Long customerId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Müşteri var mı kontrol et
            if (!customerRepository.existsById(customerId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Müşteri bulunamadı"));
            }
            
            // OCR işlemi ile fotoğraftan metin tanıma
            String ocrResult = ocrService.extractTextFromImage(file);
            
            // OCR sonucu boş mu kontrol et
            if (ocrResult == null || ocrResult.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Fotoğraftan metin tanınamadı. Lütfen daha net bir fotoğraf yükleyin."));
            }
            
            // Metin işleme ve veritabanına kaydetme (Service'te zaten kaydediliyor)
            List<Measurement> measurements = measurementService.processOcrResultForCustomer(ocrResult, customerId);
            
            if (measurements.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Fotoğraftan ölçü verisi çıkarılamadı. Lütfen ölçü verilerini içeren bir fotoğraf yükleyin."));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", measurements.size() + " ölçü başarıyla kaydedildi");
            response.put("count", measurements.size());
            response.put("ocrText", ocrResult); // Debug için OCR sonucunu da döndürelim
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ölçüler yüklenirken hata oluştu: " + e.getMessage());
            errorResponse.put("success", false);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Eski endpoint - geriye dönük uyumluluk için (deprecated)
    @PostMapping("/upload-measurements")
    @Deprecated
    public ResponseEntity<Map<String, Object>> uploadMeasurementsOld(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Bu endpoint artık kullanılmıyor. Lütfen /upload-measurements/{customerId} endpoint'ini kullanın."));
    }
} 