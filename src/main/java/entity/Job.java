package entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String company;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "job_type")
    private String jobType; // Full-time, Part-time, Contract, etc.
    
    @Column(nullable = true)
    private String salary;
    
    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;
    
    @Column(name = "experience_required")
    private String experienceRequired;
    
    @Column(name = "education_required")
    private String educationRequired;
    
    @Column(name = "posting_date")
    private LocalDate postingDate;
    
    @Column(name = "deadline_date")
    private LocalDate deadlineDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private boolean active = true;
    
    @ManyToOne
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;
    
    // Status field
    @Column(name = "status", length = 20)
    private String status = "OPEN";
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getJobType() {
        return jobType;
    }
    
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    
    public String getSalary() {
        return salary;
    }
    
    public void setSalary(String salary) {
        this.salary = salary;
    }
    
    public String getSkillsRequired() {
        return skillsRequired;
    }
    
    public void setSkillsRequired(String skillsRequired) {
        this.skillsRequired = skillsRequired;
    }
    
    public String getExperienceRequired() {
        return experienceRequired;
    }
    
    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }
    
    public String getEducationRequired() {
        return educationRequired;
    }
    
    public void setEducationRequired(String educationRequired) {
        this.educationRequired = educationRequired;
    }
    
    public LocalDate getPostingDate() {
        return postingDate;
    }
    
    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }
    
    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }
    
    public void setDeadlineDate(LocalDate deadlineDate) {
        this.deadlineDate = deadlineDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Recruiter getRecruiter() {
        return recruiter;
    }
    
    public void setRecruiter(Recruiter recruiter) {
        this.recruiter = recruiter;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
} 