package erdalguda.main.repository;

import erdalguda.main.model.Blog;
import erdalguda.main.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlug(String slug);
    
    List<Blog> findByPublishedTrue();
    
    List<Blog> findByCategoriesContainingAndPublishedTrue(Category category);
    
    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.createdAt DESC")
    List<Blog> findLatestPublishedBlogs();
    
    @Query(value = "SELECT * FROM blog WHERE published = true ORDER BY created_at DESC LIMIT ?1", nativeQuery = true)
    List<Blog> findTopNPublishedBlogs(int limit);
} 