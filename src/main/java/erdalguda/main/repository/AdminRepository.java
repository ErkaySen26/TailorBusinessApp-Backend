package erdalguda.main.repository;

import erdalguda.main.model.Admin;
import erdalguda.main.model.Admin.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRepository extends JpaRepository<Admin, String> {
    List<Admin> findByRole(Role role);
}
