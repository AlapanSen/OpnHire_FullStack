package controller;

import Repository.JobRepository;
import Repository.RecruiterRepository;
import Repository.UserRepository;
import Repository.SeekerRepository;
import Repository.SavedJobRepository;
import entity.Job;
import entity.Recruiter;
import entity.User;
import entity.Seeker;
import entity.SavedJob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private RecruiterRepository recruiterRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private SavedJobRepository savedJobRepository;
    
    // List all available jobs for job seekers
    @GetMapping("")
    public String listAllJobs(Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to view jobs";
        }
        
        // Get all active jobs
        List<Job> jobs = jobRepository.findActiveJobsWithValidRecruiters(true);
        
        // If the user is a seeker, get their saved jobs to mark them in the UI
        if (user.getRole().equals(User.UserRole.SEEKER)) {
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            if (seeker != null) {
                List<SavedJob> savedJobs = savedJobRepository.findBySeeker(seeker);
                List<Long> savedJobIds = savedJobs.stream()
                    .map(savedJob -> savedJob.getJob().getId())
                    .collect(Collectors.toList());
                model.addAttribute("savedJobIds", savedJobIds);
                model.addAttribute("seeker", seeker);
            }
        }
        
        model.addAttribute("jobs", jobs);
        model.addAttribute("user", user);
        
        return "seeker/jobs";
    }
    
    // Show job details page
    @GetMapping("/{id}")
    public String showJobDetails(@PathVariable("id") Long id, Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to view job details";
        }
        
        // Get the job
        Optional<Job> jobOpt = jobRepository.findById(id);
        
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            
            // If the user is a seeker, check if they've saved this job
            if (user.getRole().equals(User.UserRole.SEEKER)) {
                Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
                if (seeker != null) {
                    boolean isSaved = savedJobRepository.findBySeekerAndJob(seeker, job) != null;
                    model.addAttribute("isSaved", isSaved);
                    model.addAttribute("seeker", seeker);
                }
            }
            
            model.addAttribute("job", job);
            model.addAttribute("user", user);
            
            return "seeker/job-details";
        } else {
            return "redirect:/jobs?error=Job not found";
        }
    }
    
    // Show all jobs for the logged-in recruiter
    @GetMapping("/manage")
    public String manageJobs(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
        if (recruiter == null) {
            return "redirect:/login?error=Recruiter profile not found.";
        }
        
        List<Job> jobs = jobRepository.findByRecruiter(recruiter);
        
        model.addAttribute("jobs", jobs);
        return "recruiter/manage-jobs";
    }
    
    // Show job creation form - explicitly mapping to /create
    @GetMapping("/create")
    public String showCreateJobForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        // Get recruiter profile to pre-populate company info
        Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
        if (recruiter == null) {
            return "redirect:/login?error=Recruiter profile not found.";
        }
        
        // Create new job and pre-populate company info
        Job job = new Job();
        job.setCompany(recruiter.getCompany());
        job.setLocation(recruiter.getLocation());
        
        model.addAttribute("job", job);
        
        // Explicitly return the template path
        return "recruiter/create-job";
    }
    
    // Process job creation
    @PostMapping("/create")
    public String createJob(
            @ModelAttribute Job job,
            @RequestParam("deadlineDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        try {
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            if (recruiter == null) {
                return "redirect:/login?error=Recruiter profile not found.";
            }
            
            job.setRecruiter(recruiter);
            job.setPostingDate(LocalDate.now()); // Current date
            job.setDeadlineDate(deadlineDate);
            job.setActive(true);
            
            jobRepository.save(job);
            redirectAttributes.addFlashAttribute("message", "Job posted successfully!");
            return "redirect:/jobs/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating job: " + e.getMessage());
            return "redirect:/jobs/create";
        }
    }
    
    // Show job edit form
    @GetMapping("/edit/{id}")
    public String showEditJobForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            
            // Check if the job belongs to the logged-in recruiter
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            if (recruiter == null || !job.getRecruiter().getId().equals(recruiter.getId())) {
                return "redirect:/jobs/manage?error=You can only edit your own job listings";
            }
            
            model.addAttribute("job", job);
            return "recruiter/edit-job";
        } else {
            return "redirect:/jobs/manage?error=Job not found";
        }
    }
    
    // Process job update
    @PostMapping("/update/{id}")
    public String updateJob(
            @PathVariable("id") Long id,
            @ModelAttribute Job updatedJob,
            @RequestParam("deadlineDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            
            // Check if the job belongs to the logged-in recruiter
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            if (recruiter == null || !job.getRecruiter().getId().equals(recruiter.getId())) {
                return "redirect:/jobs/manage?error=You can only edit your own job listings";
            }
            
            // Update job fields
            job.setTitle(updatedJob.getTitle());
            job.setDescription(updatedJob.getDescription());
            job.setCompany(updatedJob.getCompany());
            job.setLocation(updatedJob.getLocation());
            job.setJobType(updatedJob.getJobType());
            job.setSalary(updatedJob.getSalary());
            job.setSkillsRequired(updatedJob.getSkillsRequired());
            job.setExperienceRequired(updatedJob.getExperienceRequired());
            job.setEducationRequired(updatedJob.getEducationRequired());
            job.setDeadlineDate(deadlineDate);
            job.setActive(updatedJob.isActive());
            
            jobRepository.save(job);
            redirectAttributes.addFlashAttribute("message", "Job updated successfully!");
            return "redirect:/jobs/manage";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found");
            return "redirect:/jobs/manage";
        }
    }
    
    // Toggle job active status
    @PostMapping("/toggle-status/{id}")
    public String toggleJobStatus(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            
            // Check if the job belongs to the logged-in recruiter
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            if (recruiter == null || !job.getRecruiter().getId().equals(recruiter.getId())) {
                return "redirect:/jobs/manage?error=You can only modify your own job listings";
            }
            
            // Toggle the active status
            job.setActive(!job.isActive());
            jobRepository.save(job);
            
            String status = job.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("message", "Job " + status + " successfully!");
            return "redirect:/jobs/manage";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found");
            return "redirect:/jobs/manage";
        }
    }
    
    // Delete job
    @PostMapping("/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/login?error=Access denied. Please log in as a recruiter.";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            
            // Check if the job belongs to the logged-in recruiter
            Recruiter recruiter = recruiterRepository.findById(user.getId()).orElse(null);
            if (recruiter == null || !job.getRecruiter().getId().equals(recruiter.getId())) {
                return "redirect:/jobs/manage?error=You can only delete your own job listings";
            }
            
            jobRepository.delete(job);
            redirectAttributes.addFlashAttribute("message", "Job deleted successfully!");
            return "redirect:/jobs/manage";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found");
            return "redirect:/jobs/manage";
        }
    }
} 