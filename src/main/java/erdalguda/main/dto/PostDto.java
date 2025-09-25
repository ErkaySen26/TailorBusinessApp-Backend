package erdalguda.main.dto;

import erdalguda.main.model.Category;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostDto {
    private Long id;
    private String title;
    private String urlSlug;
    private String content;
    private String featuredImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryDto> categories = new ArrayList<>();
    private Boolean published = false;
    
    // Manual getter/setter methods (Lombok @Data not working)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getUrlSlug() { return urlSlug; }
    public void setUrlSlug(String urlSlug) { this.urlSlug = urlSlug; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getFeaturedImage() { return featuredImage; }
    public void setFeaturedImage(String featuredImage) { this.featuredImage = featuredImage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<CategoryDto> getCategories() { return categories; }
    public void setCategories(List<CategoryDto> categories) { this.categories = categories; }
    
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
}