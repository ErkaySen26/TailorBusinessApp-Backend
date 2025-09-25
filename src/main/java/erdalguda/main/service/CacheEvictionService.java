package erdalguda.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CacheEvictionService {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionService.class);

    // Her gece 02:00'da tüm cache'leri temizle
    @Scheduled(cron = "0 0 2 * * ?")
    @CacheEvict(value = {"dashboard-stats", "order-status-distribution", "monthly-trend", "product-distribution", "recent-orders"}, allEntries = true)
    public void clearAllCachesDaily() {
        logger.info("🔄 Günlük cache temizleme işlemi başlatıldı");
        logger.info("✅ Tüm cache'ler başarıyla temizlendi");
    }

    // Her 6 saatte bir dashboard istatistiklerini temizle (daha güncel veriler için)
    @Scheduled(fixedRate = 21600000) // 6 saat = 6 * 60 * 60 * 1000 ms
    @CacheEvict(value = "dashboard-stats", allEntries = true)
    public void clearDashboardStatsCache() {
        logger.info("📊 Dashboard istatistikleri cache'i temizlendi (6 saatlik periyot)");
    }

    // Recent orders cache'ini her saat temizle (daha güncel veri için)
    @Scheduled(fixedRate = 3600000) // 1 saat = 60 * 60 * 1000 ms
    @CacheEvict(value = "recent-orders", allEntries = true)
    public void clearRecentOrdersCache() {
        logger.info("📋 Son siparişler cache'i temizlendi (saatlik periyot)");
    }

    // Manuel cache temizleme metodları
    @CacheEvict(value = "dashboard-stats", allEntries = true)
    public void evictDashboardStats() {
        logger.info("🎯 Dashboard istatistikleri cache'i manuel olarak temizlendi");
    }

    @CacheEvict(value = "order-status-distribution", allEntries = true)
    public void evictOrderStatusDistribution() {
        logger.info("📈 Sipariş durum dağılımı cache'i manuel olarak temizlendi");
    }

    @CacheEvict(value = "monthly-trend", allEntries = true)
    public void evictMonthlyTrend() {
        logger.info("📅 Aylık trend verileri cache'i manuel olarak temizlendi");
    }

    @CacheEvict(value = "product-distribution", allEntries = true)
    public void evictProductDistribution() {
        logger.info("🧥 Ürün dağılımı cache'i manuel olarak temizlendi");
    }

    @CacheEvict(value = "recent-orders", allEntries = true)
    public void evictRecentOrders() {
        logger.info("📝 Son siparişler cache'i manuel olarak temizlendi");
    }

    // Yeni sipariş veya müşteri eklendiğinde cache'leri temizle
    @CacheEvict(value = {"dashboard-stats", "order-status-distribution", "recent-orders"}, allEntries = true)
    public void evictOnNewOrder() {
        logger.info("🆕 Yeni sipariş nedeniyle ilgili cache'ler temizlendi");
    }

    @CacheEvict(value = {"dashboard-stats"}, allEntries = true)
    public void evictOnNewCustomer() {
        logger.info("👤 Yeni müşteri nedeniyle dashboard cache'i temizlendi");
    }

    // Sipariş durumu değiştiğinde cache temizle
    @CacheEvict(value = {"dashboard-stats", "order-status-distribution", "monthly-trend"}, allEntries = true)
    public void evictOnOrderStatusChange() {
        logger.info("🔄 Sipariş durum değişikliği nedeniyle ilgili cache'ler temizlendi");
    }
} 