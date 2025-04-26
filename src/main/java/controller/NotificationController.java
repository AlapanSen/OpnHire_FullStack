package controller;

import entity.Notification;
import entity.User;
import service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    /**
     * View all notifications for the current user
     */
    @GetMapping("/notifications")
    public String viewAllNotifications(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please login to view notifications";
        }
        
        // Get all notifications for this user
        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("notifications", notifications);
        
        return "notifications";
    }
    
    /**
     * Mark a notification as read (AJAX)
     */
    @PostMapping("/notifications/{notificationId}/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Mark notification as read
        Notification notification = notificationService.markAsRead(notificationId);
        
        if (notification != null) {
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Notification not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete a notification (AJAX)
     */
    @PostMapping("/notifications/{notificationId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long notificationId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Delete notification
            notificationService.deleteNotification(notificationId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting notification: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Mark all notifications as read (AJAX)
     */
    @PostMapping("/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Mark all notifications as read
        notificationService.markAllAsRead(user);
        
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
} 