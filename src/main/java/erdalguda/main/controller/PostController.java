package erdalguda.main.controller;

import erdalguda.main.dto.PostDto;
import erdalguda.main.model.Post;
import erdalguda.main.model.Category;
import erdalguda.main.repository.PostRepository;
import erdalguda.main.repository.CategoryRepository;
import erdalguda.main.util.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final Mapper mapper;

    @Autowired
    public PostController(PostRepository postRepository, CategoryRepository categoryRepository, Mapper mapper) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostDto> postDtos = posts.stream()
                .map(mapper::toPostDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(postDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Post>> getPostsByCategory(@PathVariable Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Post> posts = postRepository.findByCategoryId(categoryId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        try {
            // Kategorileri kontrol et ve ayarla
            if (post.getCategories() != null && !post.getCategories().isEmpty()) {
                List<Category> categories = new ArrayList<>();

                for (Category category : post.getCategories()) {
                    Optional<Category> existingCategory = categoryRepository.findById(category.getId());
                    if (existingCategory.isPresent()) {
                        categories.add(existingCategory.get());
                    }
                }

                post.setCategories(categories);
            }

            Post savedPost = postRepository.save(post);
            return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Post oluşturulurken hata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Post postDetails) {
        try {
            Optional<Post> postOptional = postRepository.findById(id);

            if (postOptional.isPresent()) {
                Post existingPost = postOptional.get();
                existingPost.setTitle(postDetails.getTitle());
                existingPost.setContent(postDetails.getContent());
                existingPost.setUrlSlug(postDetails.getUrlSlug());
                existingPost.setFeaturedImage(postDetails.getFeaturedImage());
                existingPost.setPublished(postDetails.getPublished());

                // Kategorileri güncelle
                if (postDetails.getCategories() != null) {
                    List<Category> categories = new ArrayList<>();

                    for (Category category : postDetails.getCategories()) {
                        Optional<Category> existingCategory = categoryRepository.findById(category.getId());
                        if (existingCategory.isPresent()) {
                            categories.add(existingCategory.get());
                        }
                    }

                    existingPost.setCategories(categories);
                }

                Post updatedPost = postRepository.save(existingPost);
                return new ResponseEntity<>(updatedPost, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Post güncellenirken hata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePost(@PathVariable Long id) {
        try {
            postRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String keyword) {
        List<Post> posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
} 