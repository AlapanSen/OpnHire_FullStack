package Repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE CONCAT(u.firstName, ' ', u.lastName) LIKE %:query% OR u.email LIKE %:query%")
    List<User> findByNameOrEmailContaining(@Param("query") String query);
}
