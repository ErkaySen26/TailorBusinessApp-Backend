package erdalguda.main.repository;

import erdalguda.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Onay bekleyen kullanıcılar
    List<User> findByIsApprovedFalseOrderByCreatedAtDesc();
    
    // Onaylı kullanıcılar
    List<User> findByIsApprovedTrueOrderByCreatedAtDesc();
    
    // Onay bekleyen kullanıcı sayısı
    long countByIsApprovedFalse();
} 