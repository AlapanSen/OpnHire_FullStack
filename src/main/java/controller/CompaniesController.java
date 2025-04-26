package controller;

import entity.User;
import entity.Recruiter;
import entity.Job;
import entity.Seeker;
import Repository.RecruiterRepository;
import Repository.JobRepository;
import Repository.SeekerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
@RequestMapping("/companies")
public class CompaniesController {

    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    // Show all companies
    @GetMapping
    public String listCompanies(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login?error=Please log in to view companies";
        }
        
        // Get seeker profile if the user is a seeker
        if (user.getRole().equals(User.UserRole.SEEKER)) {
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            if (seeker != null) {
                model.addAttribute("seeker", seeker);
            }
        }
        
        // Get all recruiters grouped by company
        List<Recruiter> recruiters = recruiterRepository.findAll();
        
        // Group recruiters by company name
        Map<String, List<Recruiter>> companiesMap = recruiters.stream()
                .filter(r -> r.getCompany() != null && !r.getCompany().isEmpty())
                .collect(Collectors.groupingBy(Recruiter::getCompany));
        
        // Convert to a list for display and sort alphabetically
        List<Map.Entry<String, List<Recruiter>>> companies = companiesMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
        
        model.addAttribute("companies", companies);
        
        return "seeker/company-list";
    }
    
    // Show company details
    @GetMapping("/{companyName}")
    public String showCompanyDetails(
            @PathVariable("companyName") String companyName,
            HttpSession session, 
            Model model) {
        
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login?error=Please log in to view company details";
        }
        
        // Get seeker profile if the user is a seeker
        if (user.getRole().equals(User.UserRole.SEEKER)) {
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            if (seeker != null) {
                model.addAttribute("seeker", seeker);
            }
        }
        
        // Get all recruiters for this company
        List<Recruiter> companyRecruiters = recruiterRepository.findByCompany(companyName);
        
        if (companyRecruiters.isEmpty()) {
            return "redirect:/companies?error=Company not found";
        }
        
        // Get a representative recruiter for company details
        Recruiter representative = companyRecruiters.get(0);
        
        // Get all jobs posted by this company
        List<Job> companyJobs = jobRepository.findByCompany(companyName);
        
        // Sort jobs by posting date (newest first)
        companyJobs.sort(Comparator.comparing(Job::getPostingDate).reversed());
        
        model.addAttribute("companyName", companyName);
        model.addAttribute("representative", representative);
        model.addAttribute("recruiters", companyRecruiters);
        model.addAttribute("jobs", companyJobs);
        
        return "seeker/company-details";
    }
} 