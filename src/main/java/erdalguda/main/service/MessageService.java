package erdalguda.main.service;


import erdalguda.main.dto.MessageRequest;
import erdalguda.main.dto.MessageResponse;
import erdalguda.main.model.Message;
import erdalguda.main.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageResponse saveMessage(MessageRequest messageRequest) {
        Message message = new Message(
                messageRequest.getName(),
                messageRequest.getEmail(),
                messageRequest.getContent()
        );

        Message savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }

    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getUnreadMessages() {
        return messageRepository.findByIsReadOrderByCreatedAtDesc(false)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MessageResponse getMessageById(Long id) {
        Optional<Message> messageOptional = messageRepository.findById(id);
        return messageOptional.map(this::convertToDto).orElse(null);
    }

    public MessageResponse markAsRead(Long id) {
        Optional<Message> messageOptional = messageRepository.findById(id);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setRead(true);
            Message updatedMessage = messageRepository.save(message);
            return convertToDto(updatedMessage);
        }

        return null;
    }

    public boolean deleteMessage(Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getUnreadMessageCount() {
        return messageRepository.countByIsRead(false);
    }

    private MessageResponse convertToDto(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getName(),
                message.getEmail(),
                message.getContent(),
                message.getCreatedAt(),
                message.isRead()
        );
    }
}