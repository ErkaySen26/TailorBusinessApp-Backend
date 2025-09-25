package erdalguda.main.repository;

import erdalguda.main.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    
    // Müşteriye ait tüm ölçüleri getir
    List<Measurement> findByCustomerIdOrderByRegionNameAsc(Long customerId);
    
    // Müşterinin belirli bölge ölçüsünü getir
    Optional<Measurement> findByCustomerIdAndRegionName(Long customerId, String regionName);
    
    // Müşterinin ölçü sayısını getir
    long countByCustomerId(Long customerId);
    
    // Müşteriye ait tüm ölçüleri sil
    @Modifying
    void deleteByCustomerId(Long customerId);
    
    // Belirli bölge adına sahip tüm ölçüleri getir (istatistik için)
    @Query("SELECT m FROM Measurement m WHERE m.regionName = :regionName")
    List<Measurement> findByRegionName(@Param("regionName") String regionName);
} 