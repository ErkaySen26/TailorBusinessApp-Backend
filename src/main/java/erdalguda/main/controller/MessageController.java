package erdalguda.main.controller;


import erdalguda.main.dto.MessageRequest;
import erdalguda.main.dto.MessageResponse;
import erdalguda.main.service.MessageService;
import erdalguda.main.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final EmailService emailService;

    @Autowired
    public MessageController(MessageService messageService, EmailService emailService) {
        this.messageService = messageService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageRequest messageRequest) {
        MessageResponse savedMessage = messageService.saveMessage(messageRequest);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<MessageResponse> messages = messageService.getAllMessages();
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages() {
        List<MessageResponse> unreadMessages = messageService.getUnreadMessages();
        return new ResponseEntity<>(unreadMessages, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        MessageResponse message = messageService.getMessageById(id);

        if (message != null) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        MessageResponse updatedMessage = messageService.markAsRead(id);

        if (updatedMessage != null) {
            return new ResponseEntity<>(updatedMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        boolean deleted = messageService.deleteMessage(id);

        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount() {
        long count = messageService.getUnreadMessageCount();
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> replyToMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> replyRequest,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Mesajı bul
            MessageResponse message = messageService.getMessageById(id);
            if (message == null) {
                response.put("success", false);
                response.put("message", "Mesaj bulunamadı");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Reply içeriğini al
            String subject = replyRequest.get("subject");
            String content = replyRequest.get("content");
            
            if (subject == null || subject.trim().isEmpty() || 
                content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Konu ve içerik alanları boş olamaz");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Admin adını al
            String adminName = authentication != null ? authentication.getName() : "Admin";
            
            // Email gönder
            emailService.sendMessageReplyEmail(
                message.getEmail(),
                message.getName(),
                subject,
                content,
                adminName
            );
            
            // Mesajı okundu olarak işaretle
            messageService.markAsRead(id);
            
            response.put("success", true);
            response.put("message", "Email başarıyla gönderildi");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Email gönderilirken hata oluştu: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}