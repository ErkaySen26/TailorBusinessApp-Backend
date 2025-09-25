package erdalguda.main.dto;

public class MessageRequest {
    private String name;
    private String email;
    private String content;

    // Constructors
    public MessageRequest() {
    }

    public MessageRequest(String name, String email, String content) {
        this.name = name;
        this.email = email;
        this.content = content;
    }

    // Getters and Setters
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
}