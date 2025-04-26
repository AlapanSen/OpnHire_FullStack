package Repository;

import entity.Job;
import entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    // Find jobs by recruiter
    List<Job> findByRecruiter(Recruiter recruiter);
    
    // Count jobs by recruiter
    long countByRecruiter(Recruiter recruiter);
    
    // Count jobs by recruiter and status
    long countByRecruiterAndStatus(Recruiter recruiter, String status);
    
    // Find top 5 jobs ordered by creation date descending (newest first)
    List<Job> findTop5ByOrderByCreatedAtDesc();
    
    // Find jobs by recruiter id
    List<Job> findByRecruiter_Id(Long recruiterId);
    
    // Find active jobs by recruiter
    List<Job> findByRecruiterAndActiveTrue(Recruiter recruiter);
    
    // Find jobs by title (case insensitive, contains)
    @Query("SELECT j FROM Job j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Job> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    // Find jobs by location (case insensitive, contains)
    @Query("SELECT j FROM Job j WHERE LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Job> findByLocationContainingIgnoreCase(@Param("location") String location);
    
    // Find jobs by company (case insensitive, contains)
    @Query("SELECT j FROM Job j WHERE LOWER(j.company) LIKE LOWER(CONCAT('%', :company, '%'))")
    List<Job> findByCompanyContainingIgnoreCase(@Param("company") String company);
    
    // Find active jobs posted after a specific date
    List<Job> findByActiveTrueAndPostingDateAfter(Date date);
    
    // Find jobs by job type
    List<Job> findByJobType(String jobType);
    
    // Find jobs by required skills (case insensitive, contains)
    @Query("SELECT j FROM Job j WHERE LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Job> findBySkillsRequiredContainingIgnoreCase(@Param("skill") String skill);
    
    // Find active jobs ordered by posting date descending (newest first)
    List<Job> findByActiveOrderByPostingDateDesc(boolean active);
    
    // Safe query that handles missing recruiters
    @Query("SELECT j FROM Job j JOIN FETCH j.recruiter r WHERE j.active = :active ORDER BY j.postingDate DESC")
    List<Job> findActiveJobsWithValidRecruiters(@Param("active") boolean active);
    
    // Search jobs by query string (searches title, company, location, and skills)
    @Query("SELECT DISTINCT j FROM Job j WHERE j.active = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Job> searchJobs(@Param("query") String query);

    // Find jobs by company name
    List<Job> findByCompany(String company);
} 