package controller;

import entity.Admin;
import entity.Recruiter;
import entity.Seeker;
import entity.User;
import Repository.AdminRepository;
import Repository.RecruiterRepository;
import Repository.SeekerRepository;
import Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SignupController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";  // This will resolve to templates/signup.html
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "success";  // This will resolve to templates/success.html
    }

    @PostMapping("/signup")
    public String processRegistration(
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addAttribute("error", "passwordMismatch");
                return "redirect:/signup";
            }

            // Create user entity
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setPassword(password); // In production, encrypt this!
            user.setRole(User.UserRole.valueOf(role));

            // Save user
            User savedUser = userRepository.save(user);
            
            // Create and save role-specific entity
            switch (role) {
                case "SEEKER":
                    Seeker seeker = new Seeker();
                    seeker.setUser(savedUser);
                    seekerRepository.save(seeker);
                    break;
                case "RECRUITER":
                    Recruiter recruiter = new Recruiter();
                    recruiter.setUser(savedUser);
                    recruiterRepository.save(recruiter);
                    break;
                case "ADMIN":
                    Admin admin = new Admin();
                    admin.setUser(savedUser);
                    admin.setAccessLevel("Standard");
                    admin.setSuperAdmin(false);
                    admin.setDepartment("General");
                    admin.setSecurityClearance("Level 1");
                    adminRepository.save(admin);
                    break;
            }

            // Redirect to success page
            redirectAttributes.addAttribute("role", role);
            return "redirect:/success";

        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violations
            String errorMessage;
            String errorMsg = e.getMessage().toLowerCase();
            
            if (errorMsg.contains("email")) {
                errorMessage = "emailExists";
            } else if (errorMsg.contains("phone") || errorMsg.contains("phonenumber")) {
                errorMessage = "phoneExists";
            } else if (errorMsg.contains("firstname")) {
                errorMessage = "firstNameExists";
            } else if (errorMsg.contains("lastname")) {
                errorMessage = "lastNameExists";
            } else {
                errorMessage = "duplicateEntry";
            }
            
            redirectAttributes.addAttribute("error", errorMessage);
            return "redirect:/signup";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "generalError");
            return "redirect:/signup";
        }
    }
}