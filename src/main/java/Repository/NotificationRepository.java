package Repository;

import entity.Notification;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find the 5 most recent notifications for a user
     */
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * Find all unread notifications for a user
     */
    List<Notification> findByUserAndIsReadFalse(User user);
    
    /**
     * Find notifications for a user with a limit
     */
    @Query(value = "SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsForUser(@Param("user") User user, Pageable pageable);
} 