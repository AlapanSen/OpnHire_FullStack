package controller;

import Repository.*;
import entity.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.NotificationService;
import util.JwtUtil;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private ProfileViewRepository profileViewRepository;
    
    @Autowired
    private SavedJobRepository savedJobRepository;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminRepository adminRepository;

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";  // This will resolve to templates/login.html
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        System.out.println("Dashboard GET request - User in session: " + (user != null ? user.getEmail() : "null"));
        
        if (user == null) {
            System.out.println("User not in session, redirecting to login");
            return "redirect:/login";
        }
        
        // Redirect based on user role
        System.out.println("User role: " + user.getRole() + ", redirecting to appropriate dashboard");
        
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/dashboard/admin";
            case RECRUITER:
                return "redirect:/dashboard/recruiter";
            case SEEKER:
                return "redirect:/dashboard/seeker";
            default:
                // Invalid role
                System.out.println("Invalid role: " + user.getRole() + ", invalidating session");
                session.invalidate();
                return "redirect:/login?error=Invalid role";
        }
    }
    
    @GetMapping("/dashboard/seeker")
    public String showSeekerDashboard(HttpSession session, Model model) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        System.out.println("SEEKER DASHBOARD - Session ID: " + session.getId());
        System.out.println("SEEKER DASHBOARD - User in session: " + (user != null ? user.getEmail() : "null"));
        
        if (user == null) {
            System.out.println("SEEKER DASHBOARD - No user in session, redirecting to login");
            return "redirect:/login";
        }
        
        if (user.getRole() != User.UserRole.SEEKER) {
            System.out.println("SEEKER DASHBOARD - User role is not SEEKER: " + user.getRole());
            return "redirect:/login";
        }
        
        System.out.println("SEEKER DASHBOARD - Looking up seeker profile for user ID: " + user.getId());
        Seeker seeker = seekerRepository.findByUser(user);
        
        if (seeker == null) {
            System.out.println("SEEKER DASHBOARD - No seeker profile found, redirecting to create profile");
            // Redirect to profile creation page
            return "redirect:/seeker/create-profile";
        }
        
        System.out.println("SEEKER DASHBOARD - Seeker profile found, collecting statistics");
        
        try {
            // Get statistics
            int profileViews = (int) profileViewRepository.countBySeeker(seeker);
            int savedJobs = (int) savedJobRepository.countBySeeker(seeker);
            int jobApplications = (int) jobApplicationRepository.countBySeeker(seeker);
            int profileCompletionPercentage = calculateProfileCompletionPercentage(seeker);
            
            // Get notifications
            List<Notification> notifications = notificationService.getRecentNotificationsForUser(user.getId(), 5);
            
            // Get recommended jobs (sample implementation)
            List<Job> recommendedJobs = jobRepository.findTop5ByOrderByCreatedAtDesc();
            
            System.out.println("SEEKER DASHBOARD - Adding data to model");
            
            // Add data to the model
            model.addAttribute("seeker", seeker);
            model.addAttribute("user", user);
            model.addAttribute("profileViews", profileViews);
            model.addAttribute("savedJobs", savedJobs);
            model.addAttribute("jobApplications", jobApplications);
            model.addAttribute("profileCompletionPercentage", profileCompletionPercentage);
            model.addAttribute("notifications", notifications);
            model.addAttribute("recommendedJobs", recommendedJobs);
            
            System.out.println("SEEKER DASHBOARD - Returning dashboard-seeker view");
            return "dashboard-seeker";
        } catch (Exception e) {
            System.err.println("SEEKER DASHBOARD - Error preparing dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=Error+loading+dashboard";
        }
    }
    
    private int calculateProfileCompletionPercentage(Seeker seeker) {
        int totalFields = 7; // Total fields to check
        int completedFields = 0;
        
        if (seeker.getAbout() != null && !seeker.getAbout().isEmpty()) completedFields++;
        if (seeker.getLocation() != null && !seeker.getLocation().isEmpty()) completedFields++;
        if (seeker.getProfilePic() != null && !seeker.getProfilePic().isEmpty()) completedFields++;
        if (seeker.getResume() != null && !seeker.getResume().isEmpty()) completedFields++;
        if (seeker.getLinkedinUrl() != null && !seeker.getLinkedinUrl().isEmpty()) completedFields++;
        if (seeker.getGithubUrl() != null && !seeker.getGithubUrl().isEmpty()) completedFields++;
        if (seeker.getWebsiteUrl() != null && !seeker.getWebsiteUrl().isEmpty()) completedFields++;
        
        return (int) Math.round((double) completedFields / totalFields * 100);
    }
    
    @GetMapping("/dashboard/recruiter")
    public String showRecruiterDashboard(HttpSession session, Model model) {
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.RECRUITER) {
            return "redirect:/login";
        }
        
        Recruiter recruiter = recruiterRepository.findByUser(user);
        if (recruiter == null) {
            System.out.println("RECRUITER DASHBOARD - No recruiter profile found, redirecting to create profile");
            // Redirect to profile creation page
            return "redirect:/recruiter/create-profile";
        }
        
        // Get company jobs
        List<Job> postedJobs = jobRepository.findByRecruiter(recruiter);
        
        // Get statistics
        long totalJobs = jobRepository.countByRecruiter(recruiter);
        long activeJobs = jobRepository.countByRecruiterAndStatus(recruiter, "OPEN");
        
        // Get notifications
        List<Notification> notifications = notificationService.getRecentNotificationsForUser(user.getId(), 5);
        
        // Add data to the model
        model.addAttribute("recruiter", recruiter);
        model.addAttribute("user", user);
        model.addAttribute("postedJobs", postedJobs);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("notifications", notifications);
        
        return "dashboard-recruiter";
    }
    
    @GetMapping("/dashboard/admin")
    public String redirectToAdminDashboard(HttpSession session) {
        // Check if user is logged in and is an admin
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        // Check if admin profile exists
        Admin admin = adminRepository.findByUser(user);
        if (admin == null) {
            // Create a basic admin profile
            admin = new Admin();
            admin.setUser(user);
            admin.setId(user.getId());
            admin.setAccessLevel("Standard");
            admin.setSuperAdmin(false);
            admin.setDepartment("General");
            admin.setSecurityClearance("Level 1");
            adminRepository.save(admin);
        }
        
        // Redirect to admin dashboard
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("LOGIN ATTEMPT - Email: " + email);
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                System.out.println("LOGIN FAILED - User not found: " + email);
                redirectAttributes.addFlashAttribute("error", "User not found with email: " + email);
                return "redirect:/login";
            }
            
            // Simple password check (in a real app, use proper password hashing)
            if (!user.getPassword().equals(password)) {
                System.out.println("LOGIN FAILED - Invalid password for: " + email);
                redirectAttributes.addFlashAttribute("error", "Invalid password");
                return "redirect:/login";
            }
            
            // AUTHENTICATION SUCCESS
            System.out.println("LOGIN SUCCESS - User: " + email + ", Role: " + user.getRole());
            
            // Set user in session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            
            // Debug session attributes
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Attributes:");
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                System.out.println("  " + attributeName + ": " + session.getAttribute(attributeName));
            }
            
            // Redirect based on user role
            User.UserRole role = user.getRole();
            if (role == null) {
                System.out.println("LOGIN ERROR - User has no role: " + email);
                session.invalidate();
                redirectAttributes.addFlashAttribute("error", "User has no assigned role");
                return "redirect:/login";
            }
            
            // Try direct navigation instead of redirect for SEEKER role
            if (role == User.UserRole.SEEKER) {
                System.out.println("Redirecting to regular seeker dashboard");
                return "redirect:/dashboard/seeker";
            }
            
            // Construct redirect path for other roles
            String redirectPath = "";
            switch (role) {
                case ADMIN:
                    redirectPath = "/dashboard/admin";
                    break;
                case RECRUITER:
                    redirectPath = "/dashboard/recruiter";
                    break;
                default:
                    System.out.println("LOGIN ERROR - Invalid role: " + role + " for user: " + email);
                    session.invalidate();
                    redirectAttributes.addFlashAttribute("error", "Invalid user role: " + role);
                    return "redirect:/login";
            }
            
            System.out.println("REDIRECTING TO: " + redirectPath);
            return "redirect:" + redirectPath;
        } catch (Exception e) {
            System.err.println("LOGIN ERROR - Exception: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Login error: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    /**
     * API endpoint for token-based authentication
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestParam("email") String email, 
                                     @RequestParam("password") String password) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }
            
            // Authentication manually for now
            if (!user.getPassword().equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Validate token endpoint
     */
    @PostMapping("/api/validate-token")
    @ResponseBody
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Invalid authorization header"));
            }
            
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                User user = userRepository.findByEmail(username);
                
                if (user != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("userId", user.getId());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?message=You have been logged out";
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
} 