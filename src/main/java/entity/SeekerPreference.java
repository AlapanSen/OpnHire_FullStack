package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seeker_preferences")
public class SeekerPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "seeker_id")
    private Seeker seeker;

    private Double minSalary;
    private Double maxSalary;
    private String preferredLocation;
    private String preferredJobType;
    private String preferredSkills;
    private String preferredIndustry;

    // Constructors
    public SeekerPreference() {
    }

    public SeekerPreference(Seeker seeker) {
        this.seeker = seeker;
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

    public Double getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(Double minSalary) {
        this.minSalary = minSalary;
    }

    public Double getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(Double maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getPreferredJobType() {
        return preferredJobType;
    }

    public void setPreferredJobType(String preferredJobType) {
        this.preferredJobType = preferredJobType;
    }

    public String getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(String preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public String getPreferredIndustry() {
        return preferredIndustry;
    }

    public void setPreferredIndustry(String preferredIndustry) {
        this.preferredIndustry = preferredIndustry;
    }
} 