package controller;

import entity.*;
import Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SavedJobsController {

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private SavedJobRepository savedJobRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;

    /**
     * Display saved jobs page
     */
    @GetMapping("/saved-jobs")
    public String showSavedJobs(HttpSession session, Model model) {
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
        
        // Get all saved jobs for this seeker
        List<SavedJob> savedJobs = savedJobRepository.findBySeeker(seeker);
        
        // Add data to model
        model.addAttribute("seeker", seeker);
        model.addAttribute("savedJobs", savedJobs);
        
        return "seeker/saved-jobs";
    }
    
    /**
     * Save/bookmark a job
     */
    @PostMapping("/jobs/{jobId}/save")
    @ResponseBody
    public ResponseEntity<?> toggleSaveJob(@PathVariable Long jobId, HttpSession session) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return ResponseEntity.badRequest().body("Access denied. Please log in as a job seeker.");
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            return ResponseEntity.badRequest().body("Seeker profile not found");
        }
        
        // Get job
        Job job = jobRepository.findById(jobId).orElse(null);
        
        if (job == null) {
            return ResponseEntity.badRequest().body("Job not found");
        }
        
        // Check if job is already saved
        SavedJob existingSavedJob = savedJobRepository.findBySeekerAndJob(seeker, job);
        
        if (existingSavedJob != null) {
            // Job already saved, so unsave it
            savedJobRepository.delete(existingSavedJob);
            return ResponseEntity.ok().body("{\"saved\": false}");
        } else {
            // Job not saved, so save it
            SavedJob savedJob = new SavedJob(seeker, job);
            savedJobRepository.save(savedJob);
            return ResponseEntity.ok().body("{\"saved\": true}");
        }
    }
    
    /**
     * Toggle saved job status (for AJAX requests)
     */
    @PostMapping("/saved-jobs/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleSavedJobAjax(@RequestParam Long jobId, HttpSession session) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            return ResponseEntity.badRequest().body("{\"error\": \"Access denied. Please log in as a job seeker.\"}");
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        
        if (seeker == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Seeker profile not found\"}");
        }
        
        // Get job
        Job job = jobRepository.findById(jobId).orElse(null);
        
        if (job == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Job not found\"}");
        }
        
        // Check if job is already saved
        SavedJob existingSavedJob = savedJobRepository.findBySeekerAndJob(seeker, job);
        
        if (existingSavedJob != null) {
            // Job already saved, so unsave it
            savedJobRepository.delete(existingSavedJob);
            return ResponseEntity.ok().body("{\"saved\": false}");
        } else {
            // Job not saved, so save it
            SavedJob savedJob = new SavedJob(seeker, job);
            savedJobRepository.save(savedJob);
            return ResponseEntity.ok().body("{\"saved\": true}");
        }
    }
} 