package controller;

import entity.Seeker;
import entity.User;
import Repository.SeekerRepository;
import service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class ResumeAnalyzerController {

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/resume-analyzer")
    public String showResumeAnalyzer(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please login first";
        }
        
        // Check if user is a seeker
        if (!user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/dashboard?error=Access denied. This feature is only for job seekers.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        if (seeker == null) {
            return "redirect:/dashboard?error=Seeker profile not found";
        }
        
        // Check if seeker has a resume
        if (seeker.getResume() == null || seeker.getResume().isEmpty()) {
            model.addAttribute("error", "Please upload your resume in your profile before using the analyzer");
            model.addAttribute("resumeUploaded", false);
            model.addAttribute("analysisResults", null);
            return "resume-analyzer";
        }
        
        // If resume exists but hasn't been analyzed yet
        if (!model.containsAttribute("analysisResults")) {
            model.addAttribute("resumeUploaded", true);
            model.addAttribute("fileName", seeker.getResume());
            model.addAttribute("analysisResults", null);
        }
        
        return "resume-analyzer";
    }
    
    @PostMapping("/analyze-resume")
    public String analyzeResume(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please login first";
        }
        
        // Check if user is a seeker
        if (!user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/dashboard?error=Access denied. This feature is only for job seekers.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        if (seeker == null) {
            return "redirect:/dashboard?error=Seeker profile not found";
        }
        
        try {
            // Check if seeker has a resume
            if (seeker.getResume() == null || seeker.getResume().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please upload your resume in your profile before using the analyzer");
                redirectAttributes.addFlashAttribute("resumeUploaded", false);
                redirectAttributes.addFlashAttribute("analysisResults", null);
                return "redirect:/resume-analyzer";
            }
            
            // Perform the analysis
            Map<String, Object> analysisResults = analyzeResumeFile(seeker.getResume());
            
            // Add results to model for the view
            redirectAttributes.addFlashAttribute("analysisResults", analysisResults);
            redirectAttributes.addFlashAttribute("resumeUploaded", true);
            redirectAttributes.addFlashAttribute("fileName", seeker.getResume());
            redirectAttributes.addFlashAttribute("success", "Resume analyzed successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error analyzing resume: " + e.getMessage());
            redirectAttributes.addFlashAttribute("analysisResults", null);
        }
        
        return "redirect:/resume-analyzer";
    }
    
    /**
     * Analyzes a resume file and returns insights
     */
    private Map<String, Object> analyzeResumeFile(String fileName) {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // For demonstration purposes, we'll generate mock analysis results
            // In a real application, this would integrate with a resume parsing library or API
            
            // Resume score (out of 100)
            int score = new Random().nextInt(30) + 70; // Random score between 70-99
            results.put("score", score);
            
            // Skills detected
            List<String> detectedSkills = new ArrayList<>(Arrays.asList(
                "Java", "Spring Boot", "JavaScript", "HTML/CSS", "Git", 
                "SQL", "React", "Problem Solving", "Communication"
            ));
            Collections.shuffle(detectedSkills);
            int skillCount = new Random().nextInt(5) + 3; // Random number of skills (3-7)
            results.put("skills", detectedSkills.subList(0, skillCount));
            
            // Missing keywords for job market
            List<String> missingKeywords = new ArrayList<>(Arrays.asList(
                "Docker", "Kubernetes", "AWS", "Microservices", "CI/CD",
                "TDD", "DevOps", "Agile", "REST APIs", "GraphQL"
            ));
            Collections.shuffle(missingKeywords);
            int missingCount = new Random().nextInt(3) + 2; // Random number of missing keywords (2-4)
            results.put("missingKeywords", missingKeywords.subList(0, missingCount));
            
            // Feedback on sections
            Map<String, String> sectionFeedback = new HashMap<>();
            sectionFeedback.put("Experience", "Good detail level, but consider quantifying achievements");
            sectionFeedback.put("Education", "Well structured and complete");
            sectionFeedback.put("Skills", "Consider organizing into categories (technical, soft skills)");
            sectionFeedback.put("Projects", "Add more details about your role and technologies used");
            results.put("sectionFeedback", sectionFeedback);
            
            // Job match percentage for common roles
            Map<String, Integer> jobMatches = new HashMap<>();
            jobMatches.put("Software Engineer", new Random().nextInt(15) + 80);
            jobMatches.put("Full Stack Developer", new Random().nextInt(20) + 75);
            jobMatches.put("Backend Developer", new Random().nextInt(20) + 75);
            jobMatches.put("Frontend Developer", new Random().nextInt(25) + 70);
            jobMatches.put("DevOps Engineer", new Random().nextInt(30) + 60);
            results.put("jobMatches", jobMatches);
            
        } catch (Exception e) {
            results.put("error", "Error analyzing resume: " + e.getMessage());
        }
        
        return results;
    }
} 