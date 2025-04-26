package controller;

import entity.*;
import Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RecommendedJobsController {

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private SavedJobRepository savedJobRepository;
    
    /**
     * Get recent jobs for dashboard
     */
    @GetMapping("/api/recommended-jobs")
    public String getRecommendedJobs(HttpSession session, Model model) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/login?error=Access denied. Please log in as a job seeker.";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            return "redirect:/dashboard/seeker?error=Seeker profile not found";
        }
        
        // Get all active jobs, newest first (limit to 10)
        List<Job> recentJobs = jobRepository.findActiveJobsWithValidRecruiters(true);
        if (recentJobs.size() > 10) {
            recentJobs = recentJobs.subList(0, 10);
        }
        
        // Get saved jobs for this seeker to mark them as saved
        List<SavedJob> savedJobs = savedJobRepository.findBySeeker(seeker);
        List<Long> savedJobIds = savedJobs.stream()
            .map(savedJob -> savedJob.getJob().getId())
            .collect(Collectors.toList());
        
        // Add to model
        model.addAttribute("recommendedJobs", recentJobs);
        model.addAttribute("savedJobIds", savedJobIds);
        
        return "fragments/recommended-jobs :: jobsList";
    }
} 