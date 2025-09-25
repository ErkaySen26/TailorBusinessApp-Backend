package erdalguda.main.controller;

import erdalguda.main.model.Blog;
import erdalguda.main.model.Category;
import erdalguda.main.service.BlogService;
import erdalguda.main.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;
    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

    @Autowired
    public BlogController(BlogService blogService, CategoryService categoryService) {
        this.blogService = blogService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @GetMapping("/published")
    public ResponseEntity<List<Blog>> getAllPublishedBlogs() {
        return ResponseEntity.ok(blogService.getAllPublishedBlogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(blogService.getBlogById(id));
        } catch (EntityNotFoundException e) {
            logger.error("Blog bulunamadı: " + id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Blog> getBlogBySlug(@PathVariable String slug) {
        return blogService.getBlogBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Blog>> getBlogsByCategory(@PathVariable Long categoryId) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(blogService.getBlogsByCategory(category));
        } catch (EntityNotFoundException e) {
            logger.error("Kategori bulunamadı: " + categoryId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Blog>> getLatestBlogs() {
        return ResponseEntity.ok(blogService.getLatestBlogs());
    }

    @GetMapping("/top/{count}")
    public ResponseEntity<List<Blog>> getTopBlogs(@PathVariable int count) {
        return ResponseEntity.ok(blogService.getTopNBlogs(count));
    }

    @PostMapping
    public ResponseEntity<?> createBlog(@RequestBody BlogRequest request) {
        try {
            Blog blog = new Blog();
            blog.setTitle(request.getTitle());
            blog.setContent(request.getContent());
            blog.setSlug(request.getSlug());
            blog.setImageUrl(request.getImageUrl());
            blog.setYoutubeUrl(request.getYoutubeUrl());
            blog.setMetaDescription(request.getMetaDescription());
            blog.setMetaKeywords(request.getMetaKeywords());
            blog.setPublished(request.isPublished());
            blog.setCreatedAt(LocalDateTime.now());

            Blog savedBlog = blogService.createBlog(blog, request.getCategoryIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBlog);
        } catch (EntityNotFoundException e) {
            logger.error("Blog oluşturulurken hata", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable Long id, @RequestBody BlogRequest request) {
        try {
            Blog blogDetails = new Blog();
            blogDetails.setTitle(request.getTitle());
            blogDetails.setContent(request.getContent());
            blogDetails.setSlug(request.getSlug());
            blogDetails.setImageUrl(request.getImageUrl());
            blogDetails.setYoutubeUrl(request.getYoutubeUrl());
            blogDetails.setMetaDescription(request.getMetaDescription());
            blogDetails.setMetaKeywords(request.getMetaKeywords());
            blogDetails.setPublished(request.isPublished());

            Blog updatedBlog = blogService.updateBlog(id, blogDetails, request.getCategoryIds());
            return ResponseEntity.ok(updatedBlog);
        } catch (EntityNotFoundException e) {
            logger.error("Blog güncellenirken hata: " + id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            logger.error("Blog silinirken hata: " + id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @Getter
    @Setter
    static class BlogRequest {
        private String title;
        private String content;
        private String slug;
        private String imageUrl;
        private String youtubeUrl;
        private String metaDescription;
        private String metaKeywords;
        private boolean published;
        private Set<Long> categoryIds = new HashSet<>();
        
        // Manual getters/setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getYoutubeUrl() { return youtubeUrl; }
        public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
        public String getMetaDescription() { return metaDescription; }
        public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
        public String getMetaKeywords() { return metaKeywords; }
        public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }
        public boolean isPublished() { return published; }
        public void setPublished(boolean published) { this.published = published; }
        public Set<Long> getCategoryIds() { return categoryIds; }
        public void setCategoryIds(Set<Long> categoryIds) { this.categoryIds = categoryIds; }
    }
} 