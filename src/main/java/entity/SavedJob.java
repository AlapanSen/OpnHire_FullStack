package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs")
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seeker_id")
    private Seeker seeker;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    private LocalDateTime savedTime;

    // Constructors
    public SavedJob() {
        this.savedTime = LocalDateTime.now();
    }

    public SavedJob(Seeker seeker, Job job) {
        this.seeker = seeker;
        this.job = job;
        this.savedTime = LocalDateTime.now();
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

    public LocalDateTime getSavedTime() {
        return savedTime;
    }

    public void setSavedTime(LocalDateTime savedTime) {
        this.savedTime = savedTime;
    }
} 