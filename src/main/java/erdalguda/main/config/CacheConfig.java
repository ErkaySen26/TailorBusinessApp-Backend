package erdalguda.main.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Cache isimlerini tanımla
        cacheManager.setCacheNames(List.of(
            "dashboard-stats",           // Dashboard istatistikleri
            "customer-stats",           // Müşteri istatistikleri  
            "order-stats",              // Sipariş istatistikleri
            "recent-orders",            // Son siparişler
            "order-status-distribution", // Sipariş durum dağılımı
            "monthly-trend",            // Aylık trend verileri
            "product-distribution",     // Ürün dağılımı
            "high-value-customers"      // Yüksek değerli müşteriler
        ));
        
        return cacheManager;
    }
} 