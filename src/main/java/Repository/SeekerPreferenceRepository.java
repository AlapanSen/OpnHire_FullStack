package Repository;

import entity.SeekerPreference;
import entity.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeekerPreferenceRepository extends JpaRepository<SeekerPreference, Long> {
    SeekerPreference findBySeeker(Seeker seeker);
} 