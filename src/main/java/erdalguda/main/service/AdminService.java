package erdalguda.main.service;

import erdalguda.main.model.Admin;
import erdalguda.main.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Mevcut şifreyi doğrular
     * @param username Kullanıcı adı
     * @param currentPassword Mevcut şifre
     * @return Şifre doğruysa true, değilse false
     */
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        Optional<Admin> adminOpt = adminRepository.findById(username);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        return passwordEncoder.matches(currentPassword, admin.getPassword());
    }

    /**
     * Kullanıcının şifresini değiştirir
     * @param username Kullanıcı adı
     * @param newPassword Yeni şifre
     * @return İşlem başarılıysa true, değilse false
     */
    public boolean changePassword(String username, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findById(username);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        return true;
    }
} 