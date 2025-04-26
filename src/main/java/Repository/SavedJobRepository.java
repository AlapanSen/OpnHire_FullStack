package Repository;

import entity.SavedJob;
import entity.Seeker;
import entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    // Count saved jobs for a seeker
    long countBySeeker(Seeker seeker);
    
    // Find all saved jobs for a seeker
    List<SavedJob> findBySeeker(Seeker seeker);
    
    // Find if a job is saved by a seeker
    SavedJob findBySeekerAndJob(Seeker seeker, Job job);
} 