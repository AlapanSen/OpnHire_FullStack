package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        // Add timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("timestamp", formatter.format(now));
        
        // Add message
        if (message != null && !message.toString().isEmpty()) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("message", "An unexpected error occurred");
        }
        
        // Add status and error
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("status", statusCode);
            
            switch (statusCode) {
                case 404:
                    model.addAttribute("error", "Page Not Found");
                    break;
                case 403:
                    model.addAttribute("error", "Access Denied");
                    break;
                case 400:
                    model.addAttribute("error", "Bad Request");
                    break;
                case 500:
                    model.addAttribute("error", "Internal Server Error");
                    break;
                default:
                    model.addAttribute("error", "Error Occurred");
            }
        }
        
        // Add exception details if available
        if (exception != null) {
            model.addAttribute("exception", exception.toString());
        }
        
        return "error";
    }
} 