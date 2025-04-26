package entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruiters")
public class Recruiter {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String company;
    private String location;
    private String websiteUrl;
    private String linkedinUrl;
    private String githubUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "company_logo")
    private String companyLogo;
    
    @OneToMany(mappedBy = "recruiter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getBio() {
        return bio;
    }
    
    public String getCompanyLogo() {
        return companyLogo;
    }
    
    public List<Job> getJobs() {
        return jobs;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }
    
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
    
    // Helper methods
    public void addJob(Job job) {
        jobs.add(job);
        job.setRecruiter(this);
    }
    
    public void removeJob(Job job) {
        jobs.remove(job);
        job.setRecruiter(null);
    }
}