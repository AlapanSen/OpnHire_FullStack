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

import jakarta.servlet.http.HttpSession;

@Controller
public class RecruiterProfileController {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private UserRepository userRepository;

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
                // Create new profile
                recruiter = new Recruiter();
                recruiter.setUser(user);
                recruiter.setId(user.getId()); // Explicitly set the ID to match the user ID
            }
            
            // Set profile fields
            recruiter.setCompany(company);
            recruiter.setLocation(location);
            recruiter.setBio(bio);
            recruiter.setWebsiteUrl(websiteUrl);
            recruiter.setLinkedinUrl(linkedinUrl);
            recruiter.setGithubUrl(githubUrl);
            
            // Save the profile
            recruiterRepository.save(recruiter);
            
            return "redirect:/dashboard/recruiter";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating profile: " + e.getMessage());
            return "redirect:/recruiter/create-profile";
        }
    }
} 