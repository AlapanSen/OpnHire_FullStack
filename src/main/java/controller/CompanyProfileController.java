package controller;

import entity.User;
import entity.Recruiter;
import Repository.RecruiterRepository;
import Repository.UserRepository;
import service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class CompanyProfileController {

    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    // Show company profile
    @GetMapping("/company-profile")
    public String showCompanyProfile(HttpSession session, Model model) {
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        // Get recruiter profile
        Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
        
        if (recruiter == null) {
            // Create a new recruiter profile if it doesn't exist
            recruiter = new Recruiter();
            recruiter.setUser(user);
            recruiter.setId(user.getId());
            recruiterRepository.save(recruiter);
        }
        
        model.addAttribute("recruiter", recruiter);
        model.addAttribute("uploadDir", uploadDir);
        return "recruiter/company-profile";
    }
    
    // Update company profile
    @PostMapping("/company-profile/update")
    public String updateCompanyProfile(
            @ModelAttribute Recruiter updatedRecruiter,
            @RequestParam(value = "logo", required = false) MultipartFile logoFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        try {
            // Get existing recruiter profile
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            
            if (recruiter == null) {
                return "redirect:/company-profile?error=Recruiter profile not found";
            }
            
            // Handle logo upload if a new file is provided
            if (logoFile != null && !logoFile.isEmpty()) {
                // Delete old file if exists
                if (recruiter.getCompanyLogo() != null && !recruiter.getCompanyLogo().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(recruiter.getCompanyLogo());
                    } catch (IOException e) {
                        // Log the error but continue
                        System.err.println("Failed to delete old company logo: " + e.getMessage());
                    }
                }
                
                // Store the new file
                String filename = fileStorageService.storeFile(logoFile);
                recruiter.setCompanyLogo(filename);
            }
            
            // Update recruiter fields
            recruiter.setCompany(updatedRecruiter.getCompany());
            recruiter.setLocation(updatedRecruiter.getLocation());
            recruiter.setWebsiteUrl(updatedRecruiter.getWebsiteUrl());
            recruiter.setLinkedinUrl(updatedRecruiter.getLinkedinUrl());
            recruiter.setGithubUrl(updatedRecruiter.getGithubUrl());
            recruiter.setBio(updatedRecruiter.getBio());
            
            // Save updated recruiter
            recruiterRepository.save(recruiter);
            
            redirectAttributes.addFlashAttribute("message", "Company profile updated successfully!");
            return "redirect:/company-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/company-profile";
        }
    }
} 