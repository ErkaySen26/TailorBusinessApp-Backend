package erdalguda.main.util;

import erdalguda.main.dto.CategoryDto;
import erdalguda.main.dto.PostDto;
import erdalguda.main.model.Category;
import erdalguda.main.model.Post;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class Mapper {

    public PostDto toPostDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setFeaturedImage(post.getFeaturedImage());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setPublished(post.getPublished());

        if (post.getCategories() != null) {
            dto.setCategories(post.getCategories().stream()
                    .map(this::toCategoryDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public CategoryDto toCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
} 