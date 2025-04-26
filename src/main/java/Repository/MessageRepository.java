package Repository;

import entity.Message;
import entity.Chat;
import entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find all messages in a chat
    List<Message> findByChatOrderBySentAtAsc(Chat chat);
    
    // Find messages with pagination
    Page<Message> findByChatOrderBySentAtDesc(Chat chat, Pageable pageable);
    
    // Find unread messages in a chat for a user
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId " +
           "AND m.readAt IS NULL AND m.sender.id != :userId " +
           "ORDER BY m.sentAt ASC")
    List<Message> findUnreadMessagesInChat(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    // Count unread messages in a chat for a user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId " +
           "AND m.readAt IS NULL AND m.sender.id != :userId")
    Long countUnreadMessagesInChat(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now, m.status = 'READ' " +
           "WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.readAt IS NULL")
    void markMessagesAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Find the latest message in each chat for a user
    @Query("SELECT m FROM Message m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM Message m2 WHERE m2.chat IN " +
           "(SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId) " +
           "GROUP BY m2.chat.id)")
    List<Message> findLatestMessageForUserChats(@Param("userId") Long userId);
    
    // Search messages in a chat
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY m.sentAt DESC")
    Page<Message> searchMessagesInChat(@Param("chatId") Long chatId, @Param("query") String query, Pageable pageable);
} 