package erdalguda.main.repository;


import erdalguda.main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByUrlSlug(String urlSlug);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByPublishedTrue(Pageable pageable);

    Page<Post> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.slug = :categorySlug AND p.published = true")
    Page<Post> findByCategory(String categorySlug, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> searchPosts(String keyword, Pageable pageable);

    List<Post> findByTitleContainingOrContentContaining(String title, String content);

    // Belirli bir kategoriye ait yazıları bulur
    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.id = :categoryId")
    List<Post> findByCategoryId(@Param("categoryId") Long categoryId);



}