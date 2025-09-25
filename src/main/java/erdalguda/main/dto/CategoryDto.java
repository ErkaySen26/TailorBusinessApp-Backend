package erdalguda.main.dto;

public class CategoryDto {
    private Long id;
    private String name;
    private String urlSlug;
    private String description;
    
    // Manual getter/setter methods (Lombok @Data not working)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrlSlug() { return urlSlug; }
    public void setUrlSlug(String urlSlug) { this.urlSlug = urlSlug; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}