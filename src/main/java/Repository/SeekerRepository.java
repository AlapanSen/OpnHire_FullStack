package Repository;

import entity.Seeker;
import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeekerRepository extends JpaRepository<Seeker, Long> {
    
    // Find seeker by associated user
    Seeker findByUser(User user);
    
    // Find seekers by location (partial match)
    @Query("SELECT s FROM Seeker s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Seeker> findByLocationContaining(@Param("location") String location);
    
    // Find seekers by skills (looks in about field and preference.preferredSkills)
    @Query("SELECT DISTINCT s FROM Seeker s LEFT JOIN s.preference p WHERE " +
           "LOWER(s.about) LIKE LOWER(CONCAT('%', :skill, '%')) OR " +
           "(p.preferredSkills IS NOT NULL AND LOWER(p.preferredSkills) LIKE LOWER(CONCAT('%', :skill, '%')))")
    List<Seeker> findBySkill(@Param("skill") String skill);
    
    // Find seekers by keyword in their profile (searches across multiple fields)
    @Query("SELECT DISTINCT s FROM Seeker s LEFT JOIN s.user u LEFT JOIN s.preference p WHERE " +
           "LOWER(s.about) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(p.preferredSkills IS NOT NULL AND LOWER(p.preferredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Seeker> findByKeyword(@Param("keyword") String keyword);
    
    // Advanced search with multiple optional criteria
    @Query("SELECT DISTINCT s FROM Seeker s LEFT JOIN s.user u LEFT JOIN s.preference p WHERE " +
           "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:skill IS NULL OR LOWER(s.about) LIKE LOWER(CONCAT('%', :skill, '%')) OR " +
           "(p.preferredSkills IS NOT NULL AND LOWER(p.preferredSkills) LIKE LOWER(CONCAT('%', :skill, '%')))) AND " +
           "(:keyword IS NULL OR LOWER(s.about) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Seeker> advancedSearch(@Param("location") String location, @Param("skill") String skill, @Param("keyword") String keyword);
}