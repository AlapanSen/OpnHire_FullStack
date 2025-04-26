package Repository;

import entity.Recruiter;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    // Find recruiter by user
    Recruiter findByUser(User user);
    
    // Find recruiter by user ID
    @Query("SELECT r FROM Recruiter r WHERE r.user.id = :userId")
    Recruiter findByUserId(@Param("userId") Long userId);
    
    // Find recruiters by company name
    List<Recruiter> findByCompany(String company);
}