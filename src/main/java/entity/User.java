package entity;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 45)
    private String email;

    @Column(nullable=false,unique = true,length = 20)
    private String  firstName;

    @Column(nullable=false,unique = true,length = 20)
    private String lastName;

    @Column(nullable = false,unique = true,length = 10)
    private String phoneNumber;

    @Column(nullable=false,unique = true,length = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public enum UserRole {
        SEEKER, RECRUITER, ADMIN
    }
    
    // Default constructor
    public User() {
    }

    //Getters
    public Long getId(){
        return id;
    }
    public String getEmail(){
        return email;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public String getPassword(){
        return password;
    }
    public UserRole getRole(){
        return role;
    }

    //Setters
    public void setId(Long id){
        this.id=id;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public void setFirstName(String firstName){
        this.firstName=firstName;
    }
    public void setLastName(String lastName){
        this.lastName=lastName;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber=phoneNumber;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public void setRole(UserRole role){
        this.role=role;
    }


}
