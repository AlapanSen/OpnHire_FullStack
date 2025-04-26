package controller;

import entity.User;
import entity.Seeker;
import entity.Recruiter;
import Repository.SeekerRepository;
import Repository.UserRepository;
import Repository.RecruiterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/candidates")
public class CandidateSearchController {

    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    @GetMapping("/search")
    public String showSearchPage(HttpSession session, Model model) {
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        // Add any necessary data to model for the search form
        model.addAttribute("uploadDir", uploadDir);
        
        return "recruiter/candidate-search";
    }
    
    @GetMapping("/results")
    public String searchCandidates(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpSession session, 
            Model model) {
        
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        List<Seeker> candidates;
        
        // If all search parameters are empty, return all seekers
        if ((location == null || location.trim().isEmpty()) && 
            (skill == null || skill.trim().isEmpty()) && 
            (keyword == null || keyword.trim().isEmpty())) {
            
            candidates = seekerRepository.findAll();
        } 
        // If there's only one search parameter, use the specific search method
        else if (location != null && !location.trim().isEmpty() && 
                (skill == null || skill.trim().isEmpty()) && 
                (keyword == null || keyword.trim().isEmpty())) {
            
            candidates = seekerRepository.findByLocationContaining(location.trim());
        } 
        else if (skill != null && !skill.trim().isEmpty() && 
                (location == null || location.trim().isEmpty()) && 
                (keyword == null || keyword.trim().isEmpty())) {
            
            candidates = seekerRepository.findBySkill(skill.trim());
        } 
        else if (keyword != null && !keyword.trim().isEmpty() && 
                (location == null || location.trim().isEmpty()) && 
                (skill == null || skill.trim().isEmpty())) {
            
            candidates = seekerRepository.findByKeyword(keyword.trim());
        }
        // Use advanced search for multiple criteria
        else {
            candidates = seekerRepository.advancedSearch(
                    location != null && !location.trim().isEmpty() ? location.trim() : null,
                    skill != null && !skill.trim().isEmpty() ? skill.trim() : null,
                    keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null
            );
        }
        
        // Add results to model
        model.addAttribute("candidates", candidates);
        model.addAttribute("uploadDir", uploadDir);
        model.addAttribute("location", location);
        model.addAttribute("skill", skill);
        model.addAttribute("keyword", keyword);
        model.addAttribute("resultCount", candidates.size());
        
        return "recruiter/candidate-results";
    }
} 