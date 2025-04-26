package service;

import entity.Chat;
import entity.Message;
import entity.User;
import Repository.ChatRepository;
import Repository.MessageRepository;
import Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get all chats for a user
    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findChatsByUserId(userId);
    }
    
    // Get chat by ID
    @Transactional
    public Optional<Chat> getChatById(Long chatId) {
        return chatRepository.findById(chatId);
    }
    
    // Find or create a one-to-one chat between two users
    @Transactional
    public Chat getOrCreateOneToOneChat(Long user1Id, Long user2Id) {
        // First check if chat already exists
        Optional<Chat> existingChat = chatRepository.findOneToOneChatBetweenUsers(user1Id, user2Id);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        // If not, create a new chat
        User user1 = userRepository.findById(user1Id).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("User not found"));
        
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        
        Chat newChat = new Chat(participants);
        newChat.setIsGroup(false);
        
        return chatRepository.save(newChat);
    }
    
    // Create a group chat
    @Transactional
    public Chat createGroupChat(String name, List<Long> participantIds) {
        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() < 2) {
            throw new RuntimeException("A group chat must have at least 2 participants");
        }
        
        Chat groupChat = new Chat(new HashSet<>(participants));
        groupChat.setName(name);
        groupChat.setIsGroup(true);
        
        return chatRepository.save(groupChat);
    }
    
    // Send a message
    @Transactional
    public Message sendMessage(Long chatId, Long senderId, String content, 
                              String attachmentUrl, String attachmentType, String attachmentName) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Message message = new Message(sender, content);
        message.setAttachmentUrl(attachmentUrl);
        message.setAttachmentType(attachmentType);
        message.setAttachmentName(attachmentName);
        
        chat.addMessage(message);
        chat.setUpdatedAt(LocalDateTime.now());
        
        chatRepository.save(chat);
        return message;
    }
    
    // Get messages for a chat with pagination
    public Page<Message> getChatMessages(Long chatId, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChatOrderBySentAtDesc(chat, pageable);
    }
    
    // Mark messages as read
    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        messageRepository.markMessagesAsRead(chatId, userId, now);
    }
    
    // Get unread message count for user
    public Long getUnreadMessageCount(Long userId) {
        return chatRepository.countUnreadMessagesForUser(userId);
    }
    
    // Add user to group chat
    @Transactional
    public void addUserToChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getIsGroup()) {
            throw new RuntimeException("Cannot add users to a one-to-one chat");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        chat.addParticipant(user);
        chatRepository.save(chat);
    }
    
    // Remove user from group chat
    @Transactional
    public void removeUserFromChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getIsGroup()) {
            throw new RuntimeException("Cannot remove users from a one-to-one chat");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        chat.removeParticipant(user);
        chatRepository.save(chat);
    }
    
    // Search messages in a chat
    public Page<Message> searchMessages(Long chatId, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.searchMessagesInChat(chatId, query, pageable);
    }
    
    // Get chat summary data for a user (for chat list display)
    public List<Map<String, Object>> getChatSummariesForUser(Long userId) {
        List<Chat> userChats = chatRepository.findChatsByUserId(userId);
        List<Map<String, Object>> chatSummaries = new ArrayList<>();
        
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return chatSummaries; // Return empty list if user not found
        }
        
        for (Chat chat : userChats) {
            try {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", chat.getId());
                summary.put("chatId", chat.getId()); // Keep both formats for compatibility
                
                // Get chat name or other user's name for one-to-one chats
                if (chat.getIsGroup()) {
                    summary.put("name", chat.getName());
                    summary.put("chatName", chat.getName()); // Compatibility
                    summary.put("isGroup", true);
                    summary.put("participantCount", chat.getParticipants().size());
                } else {
                    User otherUser = chat.getOtherParticipant(currentUser);
                    if (otherUser != null) {
                        String otherUserName = otherUser.getFirstName() + " " + otherUser.getLastName();
                        
                        // Create otherUser object
                        Map<String, Object> otherUserInfo = new HashMap<>();
                        otherUserInfo.put("id", otherUser.getId());
                        otherUserInfo.put("name", otherUserName);
                        otherUserInfo.put("role", otherUser.getRole());
                        
                        // Add both formats for compatibility
                        summary.put("otherUser", otherUserInfo);
                        summary.put("chatName", otherUserName); // Compatibility
                        summary.put("name", otherUserName); // Extra compatibility
                        summary.put("isGroup", false);
                        summary.put("otherUserId", otherUser.getId()); // Compatibility
                        summary.put("otherUserRole", otherUser.getRole()); // Compatibility
                    }
                }
                
                // Get latest message info
                Optional<Message> latestMessage = chat.getLatestMessage();
                if (latestMessage.isPresent()) {
                    Message message = latestMessage.get();
                    Map<String, Object> messageInfo = new HashMap<>();
                    
                    messageInfo.put("id", message.getId());
                    messageInfo.put("content", message.getContent());
                    messageInfo.put("sentAt", message.getSentAt());
                    messageInfo.put("senderName", message.getSender().getFirstName());
                    messageInfo.put("senderId", message.getSender().getId());
                    messageInfo.put("status", message.getStatus());
                    
                    if (message.hasAttachment()) {
                        messageInfo.put("hasAttachment", true);
                        messageInfo.put("attachmentType", message.getAttachmentType());
                    } else {
                        messageInfo.put("hasAttachment", false);
                    }
                    
                    summary.put("latestMessage", messageInfo);
                }
                
                // Count unread messages
                Long unreadCount = messageRepository.countUnreadMessagesInChat(chat.getId(), userId);
                summary.put("unreadCount", unreadCount);
                
                // Add to result list
                chatSummaries.add(summary);
            } catch (Exception e) {
                // Skip this chat if there's an error
                continue;
            }
        }
        
        // Sort by last updated chat first (most recent message)
        chatSummaries.sort((a, b) -> {
            Map<String, Object> messageA = (Map<String, Object>) a.get("latestMessage");
            Map<String, Object> messageB = (Map<String, Object>) b.get("latestMessage");
            
            if (messageA == null && messageB == null) {
                return 0;
            } else if (messageA == null) {
                return 1; // B comes first
            } else if (messageB == null) {
                return -1; // A comes first
            }
            
            LocalDateTime timeA = (LocalDateTime) messageA.get("sentAt");
            LocalDateTime timeB = (LocalDateTime) messageB.get("sentAt");
            
            return timeB.compareTo(timeA); // Most recent first
        });
        
        return chatSummaries;
    }
    
    // Get or create a group conversation with multiple participants
    @Transactional
    public Chat getOrCreateGroupConversation(String name, List<Long> participantIds) {
        if (participantIds.size() < 2) {
            throw new IllegalArgumentException("Group chat requires at least 2 participants");
        }
        
        // For simplicity in finding existing chats, sort participant IDs
        Collections.sort(participantIds);
        
        // Try to find an existing chat with exactly these participants
        Optional<Chat> existingChat = chatRepository.findGroupChatWithExactParticipants(participantIds);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        // If not found, create a new group chat
        return createGroupChat(name, participantIds);
    }
    
    // Search users to add to a chat (excluding current participants)
    public List<Map<String, Object>> searchUsersForChat(Long chatId, String query) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        // Get IDs of current participants to exclude them
        Set<Long> participantIds = chat.getParticipants().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        
        // Search for users by name or email
        List<User> users = userRepository.findByNameOrEmailContaining(query);
        
        // Convert to response format and filter out existing participants
        return users.stream()
                .filter(user -> !participantIds.contains(user.getId()))
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("name", user.getFirstName() + " " + user.getLastName());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("role", user.getRole());
                    return userInfo;
                })
                .collect(Collectors.toList());
    }
} 