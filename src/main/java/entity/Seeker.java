package entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "seekers")
public class Seeker {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String about;
    private String location;
    private String skills;
    private String experience;
    private String education;
    private String profilePic;
    private String resume;
    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;
    
    // Employment status field to show if seeker is currently employed
    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus = EmploymentStatus.LOOKING_FOR_WORK;
    
    // Current employer (if applicable)
    private String currentEmployer;
    
    @OneToOne(mappedBy = "seeker", cascade = CascadeType.ALL, orphanRemoval = true)
    private SeekerPreference preference;
    
    // Employment status enum
    public enum EmploymentStatus {
        EMPLOYED("Currently Employed"), 
        LOOKING_FOR_WORK("Looking for Work"),
        OPEN_TO_OPPORTUNITIES("Open to Opportunities");
        
        private final String displayName;
        
        EmploymentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return this.displayName;
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getLocation() {
        return location;
    }
    
    public String getTitle() {
        return title;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getAbout() {
        return about;
    }
    
    public String getSkills() {
        return skills;
    }
    
    public String getExperience() {
        return experience;
    }
    
    public String getEducation() {
        return education;
    }
    
    public String getResume() {
        return resume;
    }
    
    public SeekerPreference getPreference() {
        return preference;
    }
    
    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }
    
    public String getCurrentEmployer() {
        return currentEmployer;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setAbout(String about) {
        this.about = about;
    }
    
    public void setSkills(String skills) {
        this.skills = skills;
    }
    
    public void setExperience(String experience) {
        this.experience = experience;
    }
    
    public void setEducation(String education) {
        this.education = education;
    }
    
    public void setResume(String resume) {
        this.resume = resume;
    }
    
    public void setPreference(SeekerPreference preference) {
        this.preference = preference;
        if (preference != null) {
            preference.setSeeker(this);
        }
    }
    
    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }
    
    public void setCurrentEmployer(String currentEmployer) {
        this.currentEmployer = currentEmployer;
    }
    
    // Helper method to initialize preferences if not exists
    public SeekerPreference initializePreference() {
        if (this.preference == null) {
            this.preference = new SeekerPreference(this);
        }
        return this.preference;
    }
}