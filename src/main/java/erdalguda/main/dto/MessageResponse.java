package erdalguda.main.dto;

import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private String name;
    private String email;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;

    // Constructors
    public MessageResponse() {
    }

    public MessageResponse(Long id, String name, String email, String content,
                           LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}