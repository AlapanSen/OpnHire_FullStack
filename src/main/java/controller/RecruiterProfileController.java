package controller;

import entity.Recruiter;
import entity.User;
import Repository.RecruiterRepository;
import Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import jakarta.servlet.http.HttpSession;

@Controller
public class RecruiterProfileController {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/recruiter/create-profile")
    public String showCreateProfileForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.RECRUITER) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        // Check if recruiter already has a profile
        Recruiter existingRecruiter = recruiterRepository.findByUser(user);
        if (existingRecruiter != null) {
            // Pre-populate the form with existing data
            model.addAttribute("recruiter", existingRecruiter);
        }
        
        return "recruiter/create-profile";
    }

    @PostMapping("/recruiter/create-profile")
    @Transactional
    public String createRecruiterProfile(
            @RequestParam("company") String company,
            @RequestParam("location") String location,
            @RequestParam("bio") String bio,
            @RequestParam(value = "websiteUrl", required = false) String websiteUrl,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.RECRUITER) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        try {
            // Check if a recruiter profile already exists
            Recruiter existingRecruiter = recruiterRepository.findByUser(user);
            
            Recruiter recruiter;
            if (existingRecruiter != null) {
                // Update existing profile
                recruiter = existingRecruiter;
            } else {
                // Create new profile using the EntityManager
                Long userId = user.getId();
                
                // Get a fresh reference to the user
                User freshUser = entityManager.find(User.class, userId);
                if (freshUser == null) {
                    throw new IllegalStateException("User not found with ID: " + userId);
                }
                
                // Create and populate recruiter
                recruiter = new Recruiter();
                recruiter.setId(userId);
                recruiter.setUser(freshUser);
            }
            
            // Set profile fields
            recruiter.setCompany(company);
            recruiter.setLocation(location);
            recruiter.setBio(bio);
            recruiter.setWebsiteUrl(websiteUrl);
            recruiter.setLinkedinUrl(linkedinUrl);
            recruiter.setGithubUrl(githubUrl);
            
            // Save using entityManager if it's a new entity
            if (existingRecruiter == null) {
                System.out.println("Creating new recruiter with ID: " + recruiter.getId());
                entityManager.persist(recruiter);
                entityManager.flush();
            } else {
                // Update existing entity
                System.out.println("Updating existing recruiter with ID: " + recruiter.getId());
                recruiterRepository.save(recruiter);
            }
            
            System.out.println("Successfully saved recruiter profile with ID: " + recruiter.getId());
            return "redirect:/dashboard/recruiter";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating profile: " + e.getMessage());
            return "redirect:/recruiter/create-profile";
        }
    }
} 