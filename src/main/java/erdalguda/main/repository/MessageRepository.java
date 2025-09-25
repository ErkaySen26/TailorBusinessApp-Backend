package erdalguda.main.repository;

import erdalguda.main.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByOrderByCreatedAtDesc();

    List<Message> findByIsReadOrderByCreatedAtDesc(boolean isRead);

    long countByIsRead(boolean isRead);
}