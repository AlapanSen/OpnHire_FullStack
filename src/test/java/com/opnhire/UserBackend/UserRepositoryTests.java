package com.opnhire.UserBackend;

import Repository.UserRepository;
import Repository.SeekerRepository;
import entity.User;
import entity.Seeker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {
    @Autowired
    private UserRepository repo;

    @Autowired
    private SeekerRepository seekerRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testCreateUserAndSeeker(){
        // Create and save user
        User user = new User();
        user.setEmail("senalapan662@gmail.com");
        user.setPassword("alapana234");
        user.setFirstName("Alapana");
        user.setLastName("Seen");
        user.setPhoneNumber("9477557267");
        user.setRole(User.UserRole.SEEKER);

        User savedUser = repo.save(user);

        // Create and save seeker
        Seeker seeker = new Seeker();
        seeker.setUser(savedUser);
        seeker.setLocation("Kolkata");
        seeker.setLinkedinUrl("https://linkedin.com/in/alapan-sen");
        seeker.setGithubUrl("https://github.com/alapansen");
        seeker.setWebsiteUrl("https://alapansen.dev");
        seeker.setAbout("Java developer with 3 years of experience");

        Seeker savedSeeker = seekerRepo.save(seeker);

        // Verify user was saved
        User existUser = entityManager.find(User.class, savedUser.getId());
        assertThat(existUser.getEmail()).isEqualTo(user.getEmail());

        // Verify seeker was saved
        Seeker existSeeker = entityManager.find(Seeker.class, savedUser.getId());
        assertThat(existSeeker).isNotNull();
        assertThat(existSeeker.getLocation()).isEqualTo("Kolkata");
    }
}