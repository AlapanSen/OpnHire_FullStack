package controller;

import dto.ChatMessageDTO;
import entity.Message;
import entity.User;
import service.ChatService;
import service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

/**
 * Controller for handling WebSocket message interactions
 */
@Controller
public class ChatMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Handle sending messages in a chat
     * Messages are sent to /app/chat.sendMessage and broadcasted to /topic/chat/{chatId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO messageDTO) {
        try {
            Message savedMessage = chatService.sendMessage(
                messageDTO.getChatId(),
                messageDTO.getSenderId(),
                messageDTO.getContent(),
                messageDTO.getAttachmentUrl(),
                messageDTO.getAttachmentType(),
                messageDTO.getAttachmentName()
            );
            
            // Get the sender's name and add it to the DTO
            User sender = savedMessage.getSender();
            messageDTO.setId(savedMessage.getId());
            messageDTO.setTimestamp(savedMessage.getSentAt());
            messageDTO.setSenderName(sender.getFirstName() + " " + sender.getLastName());
            
            // Send to chatroom channel
            messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), messageDTO);
            
            // Also send to each participant's personal queue
            chatService.getChatById(messageDTO.getChatId()).ifPresent(chat -> {
                try {
                    chat.getParticipants().forEach(participant -> {
                        if (!participant.getId().equals(messageDTO.getSenderId())) {
                            messagingTemplate.convertAndSendToUser(
                                participant.getId().toString(), 
                                "/queue/messages", 
                                messageDTO
                            );
                        }
                    });
                } catch (Exception e) {
                    // Log error but don't fail the entire message send operation
                    System.err.println("Error sending to participant queues: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            // Create an error response to send back to the sender
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to send message: " + e.getMessage());
            errorResponse.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now()));
            
            messagingTemplate.convertAndSendToUser(
                messageDTO.getSenderId().toString(),
                "/queue/errors",
                errorResponse
            );
        }
    }
    
    /**
     * Handle user joining a chat
     * A join message is sent to /app/chat.addUser
     */
    @MessageMapping("/chat.addUser/{chatId}")
    public void addUser(@DestinationVariable Long chatId, 
                      @Payload User user,
                      SimpMessageHeaderAccessor headerAccessor) {
        
        // Add user to the WebSocket session
        headerAccessor.getSessionAttributes().put("username", user.getFirstName());
        headerAccessor.getSessionAttributes().put("userId", user.getId());
        headerAccessor.getSessionAttributes().put("chatId", chatId);
        
        // Create notification message
        Map<String, Object> joinMessage = new HashMap<>();
        joinMessage.put("type", "JOIN");
        joinMessage.put("userId", user.getId());
        joinMessage.put("username", user.getFirstName() + " " + user.getLastName());
        joinMessage.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now()));
        
        // Broadcast to all users in the chat
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, joinMessage);
        
        // Mark messages as read
        chatService.markMessagesAsRead(chatId, user.getId());
    }
    
    /**
     * Handle message read status
     */
    @MessageMapping("/chat.markRead/{chatId}")
    public void markMessagesAsRead(@DestinationVariable Long chatId, @Payload User user) {
        chatService.markMessagesAsRead(chatId, user.getId());
        
        // Notify sender that their messages were read
        Map<String, Object> readReceipt = new HashMap<>();
        readReceipt.put("type", "READ_RECEIPT");
        readReceipt.put("chatId", chatId);
        readReceipt.put("readBy", user.getId());
        readReceipt.put("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now()));
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, readReceipt);
    }
    
    /**
     * Upload file attachment for a message
     */
    @PostMapping("/api/chat/upload")
    @ResponseBody
    public Map<String, String> uploadAttachment(@RequestParam("file") MultipartFile file, 
                                             @RequestParam("chatId") Long chatId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file == null || file.isEmpty()) {
                throw new IOException("File is empty or null");
            }
            
            System.out.println("Received file upload request: " + 
                               (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed") + 
                               ", size: " + file.getSize() + 
                               ", chatId: " + chatId);
            
            String filename = fileStorageService.storeFile(file);
            String fileType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            
            System.out.println("File uploaded successfully: " + filename);
            
            response.put("success", "true");
            response.put("url", filename);
            response.put("type", fileType);
            response.put("name", originalFilename);
        } catch (Exception e) {
            System.err.println("Error in file upload: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            
            response.put("success", "false");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
} 