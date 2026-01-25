package br.com.rafaellbarros.user.domain.repository;

import br.com.rafaellbarros.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations
 */
public interface UserRepository {

    /**
     * Saves a user entity
     *
     * @param user user entity to save
     * @return saved user entity
     */
    User save(User user);

    /**
     * Finds a user by ID
     *
     * @param id user identifier
     * @return optional user entity
     */
    Optional<User> findById(UUID id);

    /**
     * Finds a user by email
     *
     * @param email user email
     * @return optional user entity
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by email
     *
     * @param email user email
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users (including inactive)
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Finds all active users
     *
     * @return list of active users
     */
    List<User> findAllActive();

    /**
     * Finds all inactive users
     *
     * @return list of inactive users
     */
    List<User> findAllInactive();

    /**
     * Finds all users including inactive (alias for findAll)
     *
     * @return list of all users
     */
    List<User> findAllWithInactive();

    /**
     * Deletes a user permanently
     *
     * @param user user entity to delete
     */
    void delete(User user);

    /**
     * Deletes a user by ID
     *
     * @param id user identifier
     */
    void deleteById(UUID id);

    /**
     * Checks if a user exists by ID
     *
     * @param id user identifier
     * @return true if user exists
     */
    boolean existsById(UUID id);

    /**
     * Counts all users
     *
     * @return total number of users
     */
    long count();

    /**
     * Counts active users
     *
     * @return number of active users
     */
    long countActive();

    /**
     * Counts inactive users
     *
     * @return number of inactive users
     */
    long countInactive();
}