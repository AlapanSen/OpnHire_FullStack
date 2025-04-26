package controller;

import entity.User;
import entity.User.UserRole;
import entity.Admin;
import entity.Recruiter;
import entity.Seeker;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.UserDataIntegrityService;
import service.UserStatisticsService;
import Repository.UserRepository;
import Repository.AdminRepository;
import Repository.RecruiterRepository;
import Repository.SeekerRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserStatisticsService userStatisticsService;
    
    @Autowired
    private UserDataIntegrityService userDataIntegrityService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;

    /**
     * Displays the admin dashboard with user statistics
     */
    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        // Add statistics to the model
        model.addAttribute("stats", userStatisticsService.getUserStatistics());
        
        return "dashboard-admin";
    }
    
    /**
     * Fixes data integrity issues by creating missing role entities
     */
    @GetMapping("/fix-user-data")
    public String fixUserData(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        // Fix data integrity issues
        String result = userDataIntegrityService.fixMissingRoleEntities();
        
        // Add result as flash attribute (will be available in the redirected request)
        redirectAttributes.addFlashAttribute("message", "Data integrity check completed. " + result);
        
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Lists all users in the system
     */
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        // Get all users from the repository
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        
        return "admin-users";
    }
    
    /**
     * Shows the form to create a new user
     */
    @GetMapping("/users/create")
    public String showCreateUserForm(HttpSession session) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        return "admin-create-user";
    }
    
    /**
     * Simple method to hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Processes the creation of a new user
     */
    @PostMapping("/users/create")
    public String createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam UserRole role,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        // Check if email already exists
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
            return "redirect:/admin/users/create";
        }
        
        try {
            // Create and save the user
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPhoneNumber(phoneNumber);
            newUser.setPassword(hashPassword(password));
            newUser.setRole(role);
            
            userRepository.save(newUser);
            
            // Create role-specific entity
            switch (role) {
                case ADMIN:
                    Admin admin = new Admin();
                    admin.setId(newUser.getId());
                    adminRepository.save(admin);
                    break;
                case RECRUITER:
                    Recruiter recruiter = new Recruiter();
                    recruiter.setUser(newUser);
                    recruiterRepository.save(recruiter);
                    break;
                case SEEKER:
                    Seeker seeker = new Seeker();
                    seeker.setUser(newUser);
                    seekerRepository.save(seeker);
                    break;
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "redirect:/admin/users/create";
        }
    }
    
    /**
     * Deletes a user
     */
    @PostMapping("/users/delete")
    public String deleteUser(
            @RequestParam Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        // Check if user is logged in and has the correct role
        if (session.getAttribute("user") == null) {
            return "redirect:/login?error=Please login first";
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }
        
        try {
            // Get the user
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
                return "redirect:/admin/users";
            }
            
            // Delete the user based on their role
            switch (user.getRole()) {
                case ADMIN:
                    adminRepository.deleteById(userId);
                    break;
                case RECRUITER:
                    recruiterRepository.deleteById(userId);
                    break;
                case SEEKER:
                    seekerRepository.deleteById(userId);
                    break;
            }
            
            // Delete the main user record
            userRepository.deleteById(userId);
            
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
} 