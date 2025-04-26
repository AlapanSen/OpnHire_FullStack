package service;

import Repository.AdminRepository;
import Repository.RecruiterRepository;
import Repository.SeekerRepository;
import Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeekerRepository seekerRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private AdminRepository adminRepository;

    /**
     * Gets counts of all user types
     * @return Map containing counts of total users, seekers, recruiters, and admins
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long seekersCount = seekerRepository.count();
        long recruitersCount = recruiterRepository.count();
        long adminsCount = adminRepository.count();
        
        stats.put("totalUsers", totalUsers);
        stats.put("seekersCount", seekersCount);
        stats.put("recruitersCount", recruitersCount);
        stats.put("adminsCount", adminsCount);
        
        return stats;
    }
} 