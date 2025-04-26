package service;

import entity.Notification;
import entity.User;
import Repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Create a notification for a user
     */
    public Notification createNotification(User user, String message, String icon) {
        Notification notification = new Notification(user, message, icon);
        return notificationRepository.save(notification);
    }
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get recent notifications for a user (limited to 5)
     */
    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get recent notifications for a user by user ID with a specified limit
     */
    public List<Notification> getRecentNotificationsForUser(Long userId, int limit) {
        try {
            if (userId == null) {
                return new ArrayList<>();
            }
            
            User user = new User();
            user.setId(userId);
            return notificationRepository.findRecentNotificationsForUser(user, PageRequest.of(0, limit));
        } catch (Exception e) {
            // Log the error but return an empty list to avoid breaking the application
            System.err.println("Error fetching notifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Count unread notifications for a user
     */
    public int countUnreadNotifications(User user) {
        return (int) notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    /**
     * Mark a notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        
        return null;
    }
    
    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
} 