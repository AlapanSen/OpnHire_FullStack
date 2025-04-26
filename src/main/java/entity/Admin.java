package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String department;
    private String accessLevel;
    private String lastLogin;
    private Boolean superAdmin;
    private String securityClearance;

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getDepartment() {
        return department;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public Boolean getSuperAdmin() {
        return superAdmin;
    }

    public String getSecurityClearance() {
        return securityClearance;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setSuperAdmin(Boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public void setSecurityClearance(String securityClearance) {
        this.securityClearance = securityClearance;
    }
}
