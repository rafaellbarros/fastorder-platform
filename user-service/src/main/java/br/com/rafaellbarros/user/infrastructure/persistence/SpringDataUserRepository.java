package br.com.rafaellbarros.user.infrastructure.persistence;

import br.com.rafaellbarros.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email
     * 
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by email
     * 
     * @param email user email
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds all active users
     * 
     * @return list of active users
     */
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.createdAt DESC")
    List<User> findAllActive();

    /**
     * Finds all inactive users
     * 
     * @return list of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.active = false ORDER BY u.createdAt DESC")
    List<User> findAllInactive();

    /**
     * Finds all users including inactive (custom query for clarity)
     * 
     * @return list of all users
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllWithInactive();

    /**
     * Counts active users
     * 
     * @return number of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActive();

    /**
     * Counts inactive users
     * 
     * @return number of inactive users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = false")
    long countInactive();

    /**
     * Finds users by status
     * 
     * @param active status
     * @return list of users
     */
    List<User> findByActive(boolean active);

    /**
     * Finds users by email containing text (case-insensitive)
     * 
     * @param email email fragment
     * @return list of users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);

    /**
     * Finds users by name containing text (case-insensitive)
     * 
     * @param name name fragment
     * @return list of users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
}