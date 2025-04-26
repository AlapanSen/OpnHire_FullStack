package service;

import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender emailSender;
    
    /**
     * Send a real email using the configured SMTP server
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            emailSender.send(message);
            
            // Log that the email was sent
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            
            // Fall back to simulation for development/debugging
            System.out.println("\n--- SIMULATED EMAIL (FALLBACK) ---");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("--- END OF EMAIL ---\n");
        }
    }
    
    /**
     * Send application status update email
     */
    public void sendApplicationStatusUpdate(User user, String jobTitle, String company, String newStatus) {
        String subject = "Your Job Application Status Update";
        String body = String.format(
            "Hello %s,\n\n" +
            "Your application for %s at %s has been updated.\n\n" +
            "New Status: %s\n\n" +
            "Log in to your account to view more details.\n\n" +
            "Best regards,\n" +
            "The OpnHire Team",
            user.getFirstName(),
            jobTitle,
            company,
            newStatus
        );
        
        sendEmail(user.getEmail(), subject, body);
    }
} 