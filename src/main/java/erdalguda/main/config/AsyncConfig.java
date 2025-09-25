package erdalguda.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Email gönderimi için optimize edilmiş ayarlar
        executor.setCorePoolSize(2);          // Temel thread sayısı
        executor.setMaxPoolSize(5);           // Maksimum thread sayısı  
        executor.setQueueCapacity(25);        // Kuyruk kapasitesi
        executor.setThreadNamePrefix("Email-");
        executor.setKeepAliveSeconds(60);     // Thread yaşam süresi
        
        // Uygulama kapanırken bekleyen taskları tamamla
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    @Bean(name = "generalTaskExecutor")
    public Executor generalTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Genel async işlemler için
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Async-");
        executor.setKeepAliveSeconds(60);
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
} 