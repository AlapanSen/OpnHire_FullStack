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
        if (user == null || user.getRole() != User.UserRole.SEEKER) {
            return "redirect:/login";
        }
        
        Seeker seeker = seekerRepository.findByUser(user);
        if (seeker == null) {
            return "redirect:/login";
        }
        
        // Get statistics
        int profileViews = (int) profileViewRepository.countBySeeker(seeker);
        int savedJobs = (int) savedJobRepository.countBySeeker(seeker);
        int jobApplications = (int) jobApplicationRepository.countBySeeker(seeker);
        int profileCompletionPercentage = calculateProfileCompletionPercentage(seeker);
        
        // Get notifications
        List<Notification> notifications = notificationService.getRecentNotificationsForUser(user.getId(), 5);
        
        // Get recommended jobs (sample implementation)
        List<Job> recommendedJobs = jobRepository.findTop5ByOrderByCreatedAtDesc();
        
        // Add data to the model
        model.addAttribute("seeker", seeker);
        model.addAttribute("user", user);
        model.addAttribute("profileViews", profileViews);
        model.addAttribute("savedJobs", savedJobs);
        model.addAttribute("jobApplications", jobApplications);
        model.addAttribute("profileCompletionPercentage", profileCompletionPercentage);
        model.addAttribute("notifications", notifications);
        model.addAttribute("recommendedJobs", recommendedJobs);
        
        return "dashboard-seeker";
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
            return "redirect:/login";
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
        // This is just a placeholder for the admin dashboard
        return "redirect:/admin";
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
            
            // Construct redirect path
            String redirectPath = "";
            switch (role) {
                case ADMIN:
                    redirectPath = "/dashboard/admin";
                    break;
                case RECRUITER:
                    redirectPath = "/dashboard/recruiter";
                    break;
                case SEEKER:
                    redirectPath = "/dashboard/seeker";
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
        // Invalidate session
        session.invalidate();
        return "redirect:/login?message=You have been logged out";
    }
    
    /**
     * API endpoint for token-based logout
     */
    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout() {
        // JWT is stateless, so we don't need to do anything server-side
        // The client should remove the token from storage
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    /**
     * Test endpoint to list users (for debugging)
     */
    @GetMapping("/test/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> testListUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("email", user.getEmail());
                userMap.put("firstName", user.getFirstName());
                userMap.put("lastName", user.getLastName());
                userMap.put("role", user.getRole());
                userList.add(userMap);
            }
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Collections.singletonMap("error", e.getMessage())));
        }
    }

    /**
     * Debug login endpoint for troubleshooting
     */
    @PostMapping("/debug-login")
    @ResponseBody
    public ResponseEntity<?> debugLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {
        
        try {
            // Step 1: Find user by email
            System.out.println("Debug login attempt for: " + email);
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "User not found", "email", email));
            }
            
            // Step 2: Check password
            boolean passwordMatches = user.getPassword().equals(password);
            
            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Password does not match", 
                                     "entered", password.replaceAll(".", "*"),
                                     "actual", user.getPassword().replaceAll(".", "*")));
            }
            
            // Step 3: Check if user has a role
            if (user.getRole() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "User has no role assigned"));
            }
            
            // Success
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole().toString()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
} 