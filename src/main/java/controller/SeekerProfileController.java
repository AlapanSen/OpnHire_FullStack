package controller;

import entity.User;
import entity.Seeker;
import entity.Recruiter;
import entity.ProfileView;
import Repository.SeekerRepository;
import Repository.UserRepository;
import Repository.RecruiterRepository;
import Repository.ProfileViewRepository;
import service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class SeekerProfileController {

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private ProfileViewRepository profileViewRepository;
    
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    // Show seeker profile
    @GetMapping("/seeker-profile")
    public String showSeekerProfile(HttpSession session, Model model) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            // Create a new seeker profile if it doesn't exist
            seeker = new Seeker();
            seeker.setUser(user);
            seeker.setId(user.getId());
            seekerRepository.save(seeker);
        }
        
        // Add seeker as a model attribute for form binding
        model.addAttribute("seeker", seeker);
        model.addAttribute("uploadDir", uploadDir);
        return "seeker/profile";
    }
    
    // Recruiter view of seeker profile
    @GetMapping("/seeker/profile/{seekerId}")
    public String viewSeekerProfile(@PathVariable("seekerId") Long seekerId, 
                                   HttpSession session, 
                                   Model model) {
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(seekerId).orElse(null);
        
        if (seeker == null) {
            return "redirect:/applications/review?error=Seeker profile not found";
        }
        
        // Get recruiter
        Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
        
        if (recruiter != null) {
            // Record the profile view
            ProfileView profileView = new ProfileView(seeker, recruiter);
            profileViewRepository.save(profileView);
        }
        
        model.addAttribute("seeker", seeker);
        model.addAttribute("uploadDir", uploadDir);
        model.addAttribute("viewMode", "recruiter"); // Indicate this is a recruiter viewing the profile
        
        return "seeker/profile-view";
    }
    
    // Update seeker profile
    @PostMapping("/seeker-profile/update")
    public String updateSeekerProfile(
            @ModelAttribute Seeker updatedSeeker,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImageFile,
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        try {
            // Get existing seeker profile
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            
            if (seeker == null) {
                return "redirect:/seeker-profile?error=Seeker profile not found";
            }
            
            // Handle profile image upload if a new file is provided
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                // Delete old file if exists
                if (seeker.getProfilePic() != null && !seeker.getProfilePic().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(seeker.getProfilePic());
                    } catch (IOException e) {
                        // Log the error but continue
                        System.err.println("Failed to delete old profile picture: " + e.getMessage());
                    }
                }
                
                // Store the new file
                String filename = fileStorageService.storeFile(profileImageFile);
                seeker.setProfilePic(filename);
            }
            
            // Handle resume upload if a new file is provided
            if (resumeFile != null && !resumeFile.isEmpty()) {
                // Check file type (allow only PDF, DOC, DOCX)
                String originalFilename = resumeFile.getOriginalFilename();
                if (originalFilename != null) {
                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                    if (!extension.equals("pdf") && !extension.equals("doc") && !extension.equals("docx")) {
                        redirectAttributes.addFlashAttribute("error", "Resume must be in PDF, DOC, or DOCX format");
                        return "redirect:/seeker-profile";
                    }
                }
                
                // Delete old resume if exists
                if (seeker.getResume() != null && !seeker.getResume().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(seeker.getResume());
                    } catch (IOException e) {
                        // Log the error but continue
                        System.err.println("Failed to delete old resume: " + e.getMessage());
                    }
                }
                
                // Store the new resume file
                String filename = fileStorageService.storeFile(resumeFile);
                seeker.setResume(filename);
            }
            
            // Update seeker fields
            seeker.setLinkedinUrl(updatedSeeker.getLinkedinUrl());
            seeker.setGithubUrl(updatedSeeker.getGithubUrl());
            seeker.setWebsiteUrl(updatedSeeker.getWebsiteUrl());
            seeker.setLocation(updatedSeeker.getLocation());
            seeker.setAbout(updatedSeeker.getAbout());
            seeker.setTitle(updatedSeeker.getTitle());
            seeker.setEmploymentStatus(updatedSeeker.getEmploymentStatus());
            seeker.setCurrentEmployer(updatedSeeker.getCurrentEmployer());
            
            // Save updated seeker
            seekerRepository.save(seeker);
            
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
            return "redirect:/seeker-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/seeker-profile";
        }
    }

    // Test endpoint for profile debugging
    @GetMapping("/test-profile")
    public String testProfile(Model model) {
        // Create a test seeker
        Seeker testSeeker = new Seeker();
        testSeeker.setId(1L);
        testSeeker.setAbout("Test about section");
        testSeeker.setLocation("Test location");
        
        // Add it as a model attribute explicitly for form binding
        model.addAttribute("seeker", testSeeker);
        model.addAttribute("uploadDir", uploadDir);
        
        return "seeker/profile";
    }

    @GetMapping("/seeker/create-profile")
    public String showCreateProfileForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SEEKER) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        // Check if seeker already has a profile
        Seeker existingSeeker = seekerRepository.findByUser(user);
        if (existingSeeker != null) {
            // Pre-populate the form with existing data
            model.addAttribute("seeker", existingSeeker);
        }
        
        return "seeker/create-profile";
    }

    @PostMapping("/seeker/create-profile")
    public String createSeekerProfile(
            @RequestParam("title") String title,
            @RequestParam("location") String location,
            @RequestParam("employmentStatus") String employmentStatus,
            @RequestParam("skills") String skills,
            @RequestParam("about") String about,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SEEKER) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        try {
            // Check if a seeker profile already exists
            Seeker existingSeeker = seekerRepository.findByUser(user);
            
            Seeker seeker;
            if (existingSeeker != null) {
                // Update existing profile
                seeker = existingSeeker;
            } else {
                // Create new profile
                seeker = new Seeker();
                seeker.setUser(user);
                seeker.setId(user.getId()); // Explicitly set the ID to match the user ID
            }
            
            // Set profile fields
            seeker.setTitle(title);
            seeker.setLocation(location);
            seeker.setEmploymentStatus(Seeker.EmploymentStatus.valueOf(employmentStatus));
            seeker.setSkills(skills);
            seeker.setAbout(about);
            
            // Save the profile
            seekerRepository.save(seeker);
            
            return "redirect:/dashboard/seeker";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating profile: " + e.getMessage());
            return "redirect:/seeker/create-profile";
        }
    }
} 