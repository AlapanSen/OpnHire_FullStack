package config;

import entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import service.NotificationService;

@Component
public class NotificationInterceptor implements HandlerInterceptor {

    @Autowired
    private NotificationService notificationService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user != null) {
            // Get the unread notification count for this user and add to session
            int unreadNotificationCount = notificationService.countUnreadNotifications(user);
            session.setAttribute("unreadNotificationCount", unreadNotificationCount);
        }
        
        return true;
    }
} 