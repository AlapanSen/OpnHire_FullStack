package service;

import Repository.AdminRepository;
import Repository.RecruiterRepository;
import Repository.SeekerRepository;
import Repository.UserRepository;
import entity.Admin;
import entity.Recruiter;
import entity.Seeker;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDataIntegrityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeekerRepository seekerRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private AdminRepository adminRepository;

    /**
     * Fixes data integrity issues by creating missing role entities for existing users
     * @return Summary of the fixes made
     */
    @Transactional
    public String fixMissingRoleEntities() {
        int seekersCreated = 0;
        int recruitersCreated = 0;
        int adminsCreated = 0;

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user.getRole() == null) {
                continue; // Skip users with no role
            }

            Long userId = user.getId();
            switch (user.getRole()) {
                case SEEKER:
                    if (seekerRepository.findById(userId).isEmpty()) {
                        Seeker seeker = new Seeker();
                        seeker.setUser(user);
                        seeker.setId(userId);
                        seekerRepository.save(seeker);
                        seekersCreated++;
                    }
                    break;
                case RECRUITER:
                    if (recruiterRepository.findById(userId).isEmpty()) {
                        Recruiter recruiter = new Recruiter();
                        recruiter.setUser(user);
                        recruiterRepository.save(recruiter);
                        recruitersCreated++;
                    }
                    break;
                case ADMIN:
                    if (adminRepository.findById(userId).isEmpty()) {
                        Admin admin = new Admin();
                        admin.setUser(user);
                        admin.setAccessLevel("Standard");
                        admin.setSuperAdmin(false);
                        admin.setDepartment("General");
                        admin.setSecurityClearance("Level 1");
                        adminRepository.save(admin);
                        adminsCreated++;
                    }
                    break;
            }
        }

        return String.format("Fixed %d missing entities: %d seekers, %d recruiters, %d admins", 
                seekersCreated + recruitersCreated + adminsCreated,
                seekersCreated, recruitersCreated, adminsCreated);
    }
} 