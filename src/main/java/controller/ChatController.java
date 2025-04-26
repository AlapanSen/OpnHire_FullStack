package controller;

import dto.ChatMessageDTO;
import entity.Chat;
import entity.Message;
import entity.User;
import entity.Seeker;
import entity.Recruiter;
import service.ChatService;
import Repository.UserRepository;
import Repository.SeekerRepository;
import Repository.RecruiterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    /**
     * Display the chat page with user's conversation list
     */
    @GetMapping
    public String showChatPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Get all chats for the current user
        List<Map<String, Object>> chatSummaries = chatService.getChatSummariesForUser(currentUser.getId());
        
        // Get unread message count
        long unreadMessageCount = chatService.getUnreadMessageCount(currentUser.getId());
        
        model.addAttribute("chats", chatSummaries);
        model.addAttribute("unreadMessageCount", unreadMessageCount);
        model.addAttribute("currentUser", currentUser);
        
        // If this is a seeker, we'll need to show the seeker profile info in the chat sidebar
        if (currentUser.getRole() == User.UserRole.SEEKER) {
            // Add seeker to the model
            try {
                Seeker seeker = seekerRepository.findById(currentUser.getId()).orElse(null);
                model.addAttribute("seeker", seeker);
            } catch (Exception e) {
                System.err.println("Error fetching seeker profile: " + e.getMessage());
            }
            model.addAttribute("viewType", "seeker");
        } else {
            // Add recruiter to the model
            try {
                Recruiter recruiter = recruiterRepository.findById(currentUser.getId()).orElse(null);
                model.addAttribute("recruiter", recruiter);
            } catch (Exception e) {
                System.err.println("Error fetching recruiter profile: " + e.getMessage());
            }
            model.addAttribute("viewType", "recruiter");
        }
        
        return "chat/chat-view";
    }
    
    /**
     * Get or create a chat with another user
     */
    @GetMapping("/conversation/{userId}")
    public String getOrCreateConversation(@PathVariable Long userId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            // Check if user exists
            User otherUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get or create conversation
            Chat chat = chatService.getOrCreateOneToOneChat(currentUser.getId(), userId);
            
            return "redirect:/chat?chatId=" + chat.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create conversation: " + e.getMessage());
            return "redirect:/chat";
        }
    }
    
    /**
     * Load a specific chat
     */
    @GetMapping("/{chatId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChat(@PathVariable Long chatId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Chat chat = chatService.getChatById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));
            
            // Verify user is a participant
            boolean isParticipant = chat.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(currentUser.getId()));
            
            if (!isParticipant) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get messages
            Page<Message> messages = chatService.getChatMessages(chatId, 0, 50);
            
            // Mark messages as read
            chatService.markMessagesAsRead(chatId, currentUser.getId());
            
            // Format response
            Map<String, Object> response = new HashMap<>();
            response.put("chatId", chat.getId());
            response.put("isGroup", chat.getIsGroup());
            
            if (chat.getIsGroup()) {
                response.put("name", chat.getName());
                
                List<Map<String, Object>> participantsList = chat.getParticipants().stream()
                        .map(p -> {
                            Map<String, Object> participant = new HashMap<>();
                            participant.put("id", p.getId());
                            participant.put("name", p.getFirstName() + " " + p.getLastName());
                            participant.put("role", p.getRole());
                            return participant;
                        })
                        .collect(Collectors.toList());
                
                response.put("participants", participantsList);
            } else {
                // For one-to-one chat, get the other user
                User otherUser = chat.getOtherParticipant(currentUser);
                response.put("otherUser", Map.of(
                    "id", otherUser.getId(),
                    "name", otherUser.getFirstName() + " " + otherUser.getLastName(),
                    "role", otherUser.getRole()
                ));
            }
            
            // Format messages
            List<Map<String, Object>> messagesList = messages.getContent().stream()
                    .map(m -> {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("id", m.getId());
                        messageMap.put("content", m.getContent());
                        messageMap.put("senderId", m.getSender().getId());
                        messageMap.put("senderName", m.getSender().getFirstName() + " " + m.getSender().getLastName());
                        messageMap.put("timestamp", m.getSentAt());
                        messageMap.put("isRead", m.isRead());
                        
                        if (m.hasAttachment()) {
                            messageMap.put("attachmentUrl", m.getAttachmentUrl());
                            messageMap.put("attachmentType", m.getAttachmentType());
                            messageMap.put("attachmentName", m.getAttachmentName());
                        }
                        
                        return messageMap;
                    })
                    .collect(Collectors.toList());
            
            response.put("messages", messagesList);
            response.put("totalMessages", messages.getTotalElements());
            response.put("currentPage", messages.getNumber());
            response.put("totalPages", messages.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create a new group chat
     */
    @PostMapping("/group")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createGroupChat(
            @RequestParam String name,
            @RequestParam List<Long> participants,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Add current user as a participant
            if (!participants.contains(currentUser.getId())) {
                participants.add(currentUser.getId());
            }
            
            Chat groupChat = chatService.createGroupChat(name, participants);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "chatId", groupChat.getId(),
                "name", groupChat.getName(),
                "participantCount", groupChat.getParticipants().size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Search users for starting a new chat
     */
    @GetMapping("/search-users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(
            @RequestParam String query,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Simple search for users
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> {
                        String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                        return fullName.contains(query.toLowerCase());
                    })
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            
            // Convert to response format
            List<Map<String, Object>> response = users.stream()
                    .map(u -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", u.getId());
                        userMap.put("name", u.getFirstName() + " " + u.getLastName());
                        userMap.put("role", u.getRole());
                        return userMap;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
    
    /**
     * Get more messages for a chat (pagination)
     */
    @GetMapping("/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Chat chat = chatService.getChatById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));
            
            // Verify user is a participant
            boolean isParticipant = chat.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(currentUser.getId()));
            
            if (!isParticipant) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get messages with pagination
            Page<Message> messages = chatService.getChatMessages(chatId, page, size);
            
            // Format messages
            List<Map<String, Object>> messagesList = messages.getContent().stream()
                    .map(m -> {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("id", m.getId());
                        messageMap.put("content", m.getContent());
                        messageMap.put("senderId", m.getSender().getId());
                        messageMap.put("senderName", m.getSender().getFirstName() + " " + m.getSender().getLastName());
                        messageMap.put("timestamp", m.getSentAt());
                        messageMap.put("isRead", m.isRead());
                        
                        if (m.hasAttachment()) {
                            messageMap.put("attachmentUrl", m.getAttachmentUrl());
                            messageMap.put("attachmentType", m.getAttachmentType());
                            messageMap.put("attachmentName", m.getAttachmentName());
                        }
                        
                        return messageMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messagesList);
            response.put("totalMessages", messages.getTotalElements());
            response.put("currentPage", messages.getNumber());
            response.put("totalPages", messages.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserConversations(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Get all chat summaries for this user
            List<Map<String, Object>> chatSummaries = chatService.getChatSummariesForUser(currentUser.getId());
            return ResponseEntity.ok(chatSummaries);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 