package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seeker_id")
    private Seeker seeker;
    
    // appliedAt field
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    private LocalDateTime appliedDate;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    
    // Store the resume that was used for this application
    // This ensures we retain the version that was used even if the seeker updates their resume later
    private String resumeUsed;

    // Application Status Enum
    public enum ApplicationStatus {
        APPLIED,
        REVIEW,
        INTERVIEW,
        REJECTED,
        ACCEPTED
    }

    // Constructors
    public JobApplication() {
        this.appliedDate = LocalDateTime.now();
        this.appliedAt = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
    }

    public JobApplication(Seeker seeker, Job job) {
        this.seeker = seeker;
        this.job = job;
        this.appliedDate = LocalDateTime.now();
        this.appliedAt = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
        // Capture the current resume at time of application
        this.resumeUsed = seeker.getResume();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Seeker getSeeker() {
        return seeker;
    }

    public void setSeeker(Seeker seeker) {
        this.seeker = seeker;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }
    
    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
    
    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public String getResumeUsed() {
        return resumeUsed;
    }
    
    public void setResumeUsed(String resumeUsed) {
        this.resumeUsed = resumeUsed;
    }
} 