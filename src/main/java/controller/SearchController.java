package controller;

import entity.Job;
import entity.Seeker;
import entity.SavedJob;
import entity.User;
import Repository.JobRepository;
import Repository.UserRepository;
import Repository.SeekerRepository;
import Repository.SavedJobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private SavedJobRepository savedJobRepository;

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query,
                        Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        
        // If no query, return empty results
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("jobs", List.of());
            model.addAttribute("query", "");
            return "search-results";
        }

        // Search for jobs by title, company name, location, or skills
        List<Job> jobs = jobRepository.searchJobs(query);
        
        // For seeker users, get their saved jobs to mark them in search results
        if (user.getRole().equals(User.UserRole.SEEKER)) {
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            if (seeker != null) {
                List<SavedJob> savedJobs = savedJobRepository.findBySeeker(seeker);
                Set<Long> savedJobIds = savedJobs.stream()
                    .map(savedJob -> savedJob.getJob().getId())
                    .collect(Collectors.toSet());
                model.addAttribute("savedJobIds", savedJobIds);
                model.addAttribute("seeker", seeker);
            }
        }
        
        model.addAttribute("jobs", jobs);
        model.addAttribute("query", query);
        
        return "search-results";
    }
} 