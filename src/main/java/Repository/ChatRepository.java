package Repository;

import entity.Chat;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    // Find all chats that a user is part of
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId ORDER BY c.updatedAt DESC")
    List<Chat> findChatsByUserId(@Param("userId") Long userId);
    
    // Find one-on-one chat between two users
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE p1.id = :user1Id AND p2.id = :user2Id AND c.isGroup = false")
    Optional<Chat> findOneToOneChatBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    // Find chats with unread messages for a user
    @Query("SELECT DISTINCT c FROM Chat c JOIN c.messages m " +
           "JOIN c.participants p WHERE p.id = :userId " +
           "AND m.readAt IS NULL AND m.sender.id != :userId")
    List<Chat> findChatsWithUnreadMessages(@Param("userId") Long userId);
    
    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM Message m JOIN m.chat c " +
           "JOIN c.participants p WHERE p.id = :userId " +
           "AND m.readAt IS NULL AND m.sender.id != :userId")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);
    
    // Find a group chat with exactly the specified participants
    @Query("SELECT c FROM Chat c WHERE c.isGroup = true " +
           "AND (SELECT COUNT(p) FROM c.participants p) = :#{#participantIds.size()} " +
           "AND NOT EXISTS (SELECT p FROM c.participants p WHERE p.id NOT IN :participantIds)")
    Optional<Chat> findGroupChatWithExactParticipants(@Param("participantIds") List<Long> participantIds);
} 