package br.com.rafaellbarros.user.infrastructure.persistence;

import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of UserRepository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        User savedUser = springDataRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return springDataRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking existence by email: {}", email);
        return springDataRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return springDataRepository.findAllActive();
    }

    @Override
    public List<User> findAllActive() {
        log.debug("Finding all active users");
        return springDataRepository.findAllActive();
    }

    @Override
    public List<User> findAllInactive() {
        log.debug("Finding all inactive users");
        return springDataRepository.findAllInactive();
    }

    @Override
    public List<User> findAllWithInactive() {
        log.debug("Finding all users including inactive");
        return springDataRepository.findAllWithInactive();
    }

    @Override
    public void delete(User user) {
        log.debug("Deleting user with ID: {}", user.getId());
        springDataRepository.delete(user);
        log.info("User deleted successfully with ID: {}", user.getId());
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting user by ID: {}", id);
        springDataRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsById(UUID id) {
        log.debug("Checking existence by ID: {}", id);
        return springDataRepository.existsById(id);
    }

    @Override
    public long count() {
        log.debug("Counting all users");
        return springDataRepository.count();
    }

    @Override
    public long countActive() {
        log.debug("Counting active users");
        return springDataRepository.countActive();
    }

    @Override
    public long countInactive() {
        log.debug("Counting inactive users");
        return springDataRepository.countInactive();
    }
}