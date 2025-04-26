package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String attachmentUrl;
    private String attachmentType;
    private String attachmentName;
    
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;
    
    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
    
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
    
    // Constructors
    public Message() {
    }
    
    public Message(User sender, String content) {
        this.sender = sender;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Chat getChat() {
        return chat;
    }
    
    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAttachmentUrl() {
        return attachmentUrl;
    }
    
    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
    
    public String getAttachmentType() {
        return attachmentType;
    }
    
    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }
    
    public String getAttachmentName() {
        return attachmentName;
    }
    
    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public MessageStatus getStatus() {
        return status;
    }
    
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
    
    // Helper methods
    public boolean isRead() {
        return this.readAt != null;
    }
    
    public boolean hasAttachment() {
        return this.attachmentUrl != null && !this.attachmentUrl.isEmpty();
    }
    
    public void markAsRead() {
        this.status = MessageStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsDelivered() {
        this.status = MessageStatus.DELIVERED;
    }
} 