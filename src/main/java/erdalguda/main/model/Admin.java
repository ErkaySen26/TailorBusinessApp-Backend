package erdalguda.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Admin {
    @Id
    private String username;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USTA; // Varsayılan olarak USTA
    
    public enum Role {
        ADMIN,        // Tam yetki - Tüm modüllere erişim
        USTA,         // Orta yetki - Müşteriler, Siparişler, Kumaşlar, Şablonlar
        MUHASEBECI    // Sınırlı yetki - Sadece Müşteriler ve Siparişler
    }

    // Manual getter/setter methods (Lombok not working)
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}