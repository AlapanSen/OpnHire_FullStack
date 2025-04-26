package Repository;

import entity.ProfileView;
import entity.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    
    // Find all views for a specific seeker
    List<ProfileView> findBySeeker(Seeker seeker);
    
    // Count total views for a seeker
    long countBySeeker(Seeker seeker);
    
    // Count views for a seeker within the last week
    @Query("SELECT COUNT(pv) FROM ProfileView pv WHERE pv.seeker = :seeker AND pv.viewTime >= :weekAgo")
    long countWeeklyViewsBySeeker(@Param("seeker") Seeker seeker, @Param("weekAgo") LocalDateTime weekAgo);
} 