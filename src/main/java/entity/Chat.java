package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; // For group chats
    private Boolean isGroup = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "chat_participants",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
    
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Chat() {
    }
    
    public Chat(Set<User> participants) {
        this.participants = participants;
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
    
    public Boolean getIsGroup() {
        return isGroup;
    }
    
    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<User> getParticipants() {
        return participants;
    }
    
    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    // Helper methods
    public void addParticipant(User user) {
        this.participants.add(user);
    }
    
    public void removeParticipant(User user) {
        this.participants.remove(user);
    }
    
    public void addMessage(Message message) {
        this.messages.add(message);
        message.setChat(this);
    }
    
    public User getOtherParticipant(User currentUser) {
        if (participants.size() == 2) {
            return participants.stream()
                    .filter(p -> !p.getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    public Message getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
    
    public Optional<Message> getLatestMessage() {
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(messages.get(messages.size() - 1));
    }
} 