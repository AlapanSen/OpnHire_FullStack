package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_views")
public class ProfileView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seeker_id")
    private Seeker seeker;

    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private Recruiter recruiter;

    @Column(nullable = false)
    private LocalDateTime viewTime;

    // Constructors
    public ProfileView() {
        this.viewTime = LocalDateTime.now();
    }

    public ProfileView(Seeker seeker, Recruiter recruiter) {
        this.seeker = seeker;
        this.recruiter = recruiter;
        this.viewTime = LocalDateTime.now();
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

    public Recruiter getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(Recruiter recruiter) {
        this.recruiter = recruiter;
    }

    public LocalDateTime getViewTime() {
        return viewTime;
    }

    public void setViewTime(LocalDateTime viewTime) {
        this.viewTime = viewTime;
    }
} 