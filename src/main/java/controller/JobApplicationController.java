package controller;

import entity.Job;
import entity.JobApplication;
import entity.Seeker;
import entity.User;
import entity.Recruiter;
import Repository.JobApplicationRepository;
import Repository.JobRepository;
import Repository.SeekerRepository;

import service.NotificationService;
import service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class JobApplicationController {

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private SeekerRepository seekerRepository;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    /**
     * Display job details and application form
     */
    @GetMapping("/job-applications/{jobId}")
    public String showJobDetails(@PathVariable("jobId") Long jobId, Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to view job details";
        }
        
        // Get job details
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return "redirect:/dashboard/seeker?error=Job not found";
        }
        
        Job job = jobOpt.get();
        model.addAttribute("job", job);
        
        // If user is a seeker, check if they have already applied
        if (user.getRole().equals(User.UserRole.SEEKER)) {
            Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
            if (seeker != null) {
                // Check if already applied
                List<JobApplication> applications = jobApplicationRepository.findByJobAndSeeker(job, seeker);
                boolean alreadyApplied = !applications.isEmpty();
                model.addAttribute("alreadyApplied", alreadyApplied);
                
                if (alreadyApplied) {
                    model.addAttribute("application", applications.get(0));
                }
            }
        }
        
        return "seeker/job-details";
    }
    
    /**
     * Process job application
     */
    @PostMapping("/jobs/{jobId}/apply")
    public String applyForJob(
            @PathVariable("jobId") Long jobId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to apply for jobs";
        }
        
        if (!user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/dashboard?error=Only job seekers can apply for jobs";
        }
        
        // Get job details
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found");
            return "redirect:/dashboard/seeker";
        }
        
        Job job = jobOpt.get();
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        if (seeker == null) {
            redirectAttributes.addFlashAttribute("error", "Please complete your profile before applying");
            return "redirect:/seeker-profile";
        }
        
        // Check if the seeker has uploaded a resume
        if (seeker.getResume() == null || seeker.getResume().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please upload your resume before applying");
            return "redirect:/seeker-profile";
        }
        
        // Check if already applied
        List<JobApplication> existingApplications = jobApplicationRepository.findByJobAndSeeker(job, seeker);
        if (!existingApplications.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You have already applied for this job");
            return "redirect:/jobs/" + jobId;
        }
        
        // Create job application with resume
        JobApplication application = new JobApplication(seeker, job);
        application.setResumeUsed(seeker.getResume());
        jobApplicationRepository.save(application);
        
        redirectAttributes.addFlashAttribute("message", "Application submitted successfully!");
        return "redirect:/jobs/" + jobId;
    }
    
    /**
     * View all applications for a seeker
     */
    @GetMapping("/applications")
    public String viewApplications(Model model, HttpSession session) {
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to view your applications";
        }
        
        if (!user.getRole().equals(User.UserRole.SEEKER)) {
            return "redirect:/dashboard?error=Access denied";
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        if (seeker == null) {
            return "redirect:/seeker-profile?error=Profile not found";
        }
        
        // Get all applications for this seeker
        List<JobApplication> applications = jobApplicationRepository.findBySeekerOrderByAppliedDateDesc(seeker);
        model.addAttribute("applications", applications);
        
        return "seeker/applications";
    }
    
    /**
     * Cancel a job application (AJAX)
     */
    @PostMapping("/applications/{applicationId}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelApplication(
            @PathVariable("applicationId") Long applicationId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is logged in and is a seeker
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(User.UserRole.SEEKER)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Get seeker profile
        Seeker seeker = seekerRepository.findById(user.getId()).orElse(null);
        if (seeker == null) {
            response.put("success", false);
            response.put("message", "Profile not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Get application
        Optional<JobApplication> applicationOpt = jobApplicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Application not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        JobApplication application = applicationOpt.get();
        
        // Check if the application belongs to this seeker
        if (!application.getSeeker().getId().equals(seeker.getId())) {
            response.put("success", false);
            response.put("message", "You can only cancel your own applications");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if application is still in APPLIED or REVIEW status
        if (application.getStatus() != JobApplication.ApplicationStatus.APPLIED && 
            application.getStatus() != JobApplication.ApplicationStatus.REVIEW) {
            response.put("success", false);
            response.put("message", "Cannot cancel application in " + application.getStatus() + " status");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Delete the application
        jobApplicationRepository.delete(application);
        
        response.put("success", true);
        response.put("message", "Application cancelled successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Recruiter: View all applications for review
     */
    @GetMapping("/applications/review")
    public String reviewApplications(Model model, HttpSession session) {
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=Please log in to review applications";
        }
        
        if (!user.getRole().equals(User.UserRole.RECRUITER)) {
            return "redirect:/dashboard?error=Access denied";
        }
        
        // Get all jobs posted by this recruiter
        List<Job> recruiterJobs = jobRepository.findByRecruiter_Id(user.getId());
        
        // Get all applications for these jobs
        List<JobApplication> applications = new ArrayList<>();
        for (Job job : recruiterJobs) {
            applications.addAll(jobApplicationRepository.findByJob(job));
        }
        
        // Sort applications by date (newest first)
        applications.sort(Comparator.comparing(JobApplication::getAppliedDate).reversed());
        
        model.addAttribute("applications", applications);
        model.addAttribute("jobs", recruiterJobs);
        
        return "recruiter/review-applications";
    }
    
    /**
     * Update application status (AJAX)
     */
    @PostMapping("/applications/{applicationId}/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable("applicationId") Long applicationId,
            @RequestBody Map<String, String> statusUpdate,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Get application
        Optional<JobApplication> applicationOpt = jobApplicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Application not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        JobApplication application = applicationOpt.get();
        
        // Check if the job belongs to this recruiter
        if (!application.getJob().getRecruiter().getId().equals(user.getId())) {
            response.put("success", false);
            response.put("message", "You can only update status for your own job postings");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Parse and validate new status
        String newStatusStr = statusUpdate.get("status");
        JobApplication.ApplicationStatus newStatus;
        try {
            newStatus = JobApplication.ApplicationStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid status value");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update status
        JobApplication.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        jobApplicationRepository.save(application);
        
        // Create notification for the seeker
        User seekerUser = application.getSeeker().getUser();
        String notificationMessage = String.format(
            "Your application for %s has been updated to %s", 
            application.getJob().getTitle(), 
            newStatus.toString()
        );
        
        String icon = getStatusIcon(newStatus);
        notificationService.createNotification(seekerUser, notificationMessage, icon);
        
        // Send email notification
        emailService.sendApplicationStatusUpdate(
            seekerUser, 
            application.getJob().getTitle(), 
            application.getJob().getCompany(),
            newStatus.toString()
        );
        
        response.put("success", true);
        response.put("message", "Status updated successfully");
        response.put("oldStatus", oldStatus.toString());
        response.put("newStatus", newStatus.toString());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to get an appropriate icon for each status
     */
    private String getStatusIcon(JobApplication.ApplicationStatus status) {
        switch (status) {
            case APPLIED:
                return "📝";
            case REVIEW:
                return "👁️";
            case INTERVIEW:
                return "🗓️";
            case ACCEPTED:
                return "✅";
            case REJECTED:
                return "❌";
            default:
                return "📢";
        }
    }

    /**
     * Download resume for a job application
     */
    @GetMapping("/applications/{applicationId}/download-resume")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable("applicationId") Long applicationId,
            HttpSession session) throws IOException {
        
        // Check if user is logged in and is a recruiter
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(User.UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Get application
        Optional<JobApplication> applicationOpt = jobApplicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        JobApplication application = applicationOpt.get();
        
        // Check if the job belongs to this recruiter
        if (!application.getJob().getRecruiter().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Check if resume exists
        String resumeFilename = application.getResumeUsed();
        if (resumeFilename == null || resumeFilename.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Load resume file
        Path resumePath = Paths.get(uploadDir).resolve(resumeFilename).normalize();
        Resource resource = new UrlResource(resumePath.toUri());
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        // Determine content type
        String contentType = "application/octet-stream";
        if (resumeFilename.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (resumeFilename.endsWith(".doc")) {
            contentType = "application/msword";
        } else if (resumeFilename.endsWith(".docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        
        // Build response with appropriate headers for download
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
} 