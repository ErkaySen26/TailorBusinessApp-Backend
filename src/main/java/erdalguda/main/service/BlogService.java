package erdalguda.main.service;

import erdalguda.main.model.Blog;
import erdalguda.main.model.Category;
import erdalguda.main.repository.BlogRepository;
import erdalguda.main.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BlogService(BlogRepository blogRepository, CategoryRepository categoryRepository) {
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByPublishedTrue();
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog bulunamadı: " + id));
    }

    public Optional<Blog> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    public List<Blog> getBlogsByCategory(Category category) {
        return blogRepository.findByCategoriesContainingAndPublishedTrue(category);
    }

    public List<Blog> getLatestBlogs() {
        return blogRepository.findLatestPublishedBlogs();
    }

    public List<Blog> getTopNBlogs(int count) {
        return blogRepository.findTopNPublishedBlogs(count);
    }

    @Transactional
    public Blog createBlog(Blog blog, Set<Long> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = categoryIds.stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Kategori bulunamadı: " + id)))
                    .collect(Collectors.toSet());
            blog.setCategories(categories);
        }
        blog.setCreatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    @Transactional
    public Blog updateBlog(Long id, Blog blogDetails, Set<Long> categoryIds) {
        Blog blog = getBlogById(id);
        blog.setTitle(blogDetails.getTitle());
        blog.setContent(blogDetails.getContent());
        blog.setSlug(blogDetails.getSlug());
        blog.setPublished(blogDetails.isPublished());
        
        if (blogDetails.getImageUrl() != null && !blogDetails.getImageUrl().isEmpty()) {
            blog.setImageUrl(blogDetails.getImageUrl());
        }
        
        if (blogDetails.getYoutubeUrl() != null) {
            blog.setYoutubeUrl(blogDetails.getYoutubeUrl());
        }
        
        if (blogDetails.getMetaDescription() != null) {
            blog.setMetaDescription(blogDetails.getMetaDescription());
        }
        
        if (blogDetails.getMetaKeywords() != null) {
            blog.setMetaKeywords(blogDetails.getMetaKeywords());
        }

        if (categoryIds != null) {
            Set<Category> categories = new HashSet<>();
            categoryIds.forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Kategori bulunamadı: " + categoryId));
                categories.add(category);
            });
            blog.setCategories(categories);
        }

        return blogRepository.save(blog);
    }

    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = getBlogById(id);
        blogRepository.delete(blog);
    }
} 