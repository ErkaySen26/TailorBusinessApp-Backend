package erdalguda.main.config;

import erdalguda.main.security.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("SecurityConfig yapılandırması başlatılıyor - 3 ROL SİSTEMİ");
        
        http.cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                try {
                    auth
                        // Genel erişim - login, test endpoint'leri
                        .requestMatchers("/auth/login", "/auth/register", "/auth/change-password").permitAll()
                        .requestMatchers("/auth/debug/**").permitAll() // Debug endpoint'leri
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/upload/test").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/messages").permitAll() // Contact form için mesaj gönderme
                        
                        // Blog ve Kategori - Public okuma, Admin yazma
                        .requestMatchers("/api/blogs/published", "/api/blogs/latest", "/api/blogs/top/**").permitAll()
                        .requestMatchers("/api/blogs/slug/**", "/api/blogs/category/**").permitAll()
                        .requestMatchers("/api/categories", "/api/categories/slug/**").permitAll()
                        .requestMatchers("/api/blogs/**").hasRole("ADMIN") // Blog yönetimi sadece admin
                        .requestMatchers("/api/categories/**").hasRole("ADMIN") // Kategori yönetimi sadece admin
                        
                        // Sadece ADMIN erişimi
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/messages/**").hasRole("ADMIN") // Mesaj yönetimi sadece admin
                        
                        // USTA erişimi (ADMIN + USTA)
                        .requestMatchers("/api/usta/**").hasAnyRole("ADMIN", "USTA")
                        
                        // MUHASEBECI erişimi (ADMIN + USTA + MUHASEBECI)
                        .requestMatchers("/api/customers", "/api/customers/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/orders", "/api/orders/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/measurements", "/api/measurements/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/reports", "/api/reports/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/upload", "/api/upload/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/muhasebeci", "/api/muhasebeci/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        .requestMatchers("/api/dashboard", "/api/dashboard/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        
                        // Genel ayarlar - tüm roller erişebilir
                        .requestMatchers("/api/settings/**").hasAnyRole("ADMIN", "USTA", "MUHASEBECI")
                        
                        // Diğer tüm istekler kimlik doğrulama gerektirir
                        .anyRequest().authenticated();
                        
                    logger.info("Yetkilendirme kuralları yapılandırıldı:");
                    logger.info("- ADMIN: Tüm modüller");
                    logger.info("- USTA: Müşteriler, Siparişler");
                    logger.info("- MUHASEBECI: Müşteriler, Siparişler");
                } catch (Exception e) {
                    logger.error("SecurityConfig yapılandırma hatası: {}", e.getMessage(), e);
                    throw e;
                }
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain yapılandırması tamamlandı - JWT Filter aktif");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Development için tüm localhost portlarına izin ver
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", 
            "https://erdalguda.netlify.app",
            "https://*.netlify.app",
            "https://erdalguda.com",
            "https://www.erdalguda.com"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        logger.info("CORS yapılandırması tamamlandı - Tüm localhost portları ve Netlify için izin verildi");
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
