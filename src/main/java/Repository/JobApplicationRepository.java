package Repository;

import entity.JobApplication;
import entity.Seeker;
import entity.Recruiter;
import entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    // Count applications for a seeker
    long countBySeeker(Seeker seeker);
    
    // Get most recent applications for a seeker
    List<JobApplication> findTop5BySeekerOrderByAppliedAtDesc(Seeker seeker);
    
    // Count applications for jobs posted by a specific recruiter
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.recruiter = :recruiter")
    long countByJobRecruiter(@Param("recruiter") Recruiter recruiter);
    
    // Count applications for a job
    long countByJob(Job job);
    
    // Find applications by job and seeker
    List<JobApplication> findByJobAndSeeker(Job job, Seeker seeker);
    
    // Find all applications for a seeker ordered by date
    List<JobApplication> findBySeekerOrderByAppliedDateDesc(Seeker seeker);
    
    // Find all applications for a specific job
    List<JobApplication> findByJob(Job job);
} 