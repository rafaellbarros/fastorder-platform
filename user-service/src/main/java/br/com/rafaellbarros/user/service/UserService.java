package br.com.rafaellbarros.user.service;

import br.com.rafaellbarros.user.domain.exception.BusinessException;
import br.com.rafaellbarros.user.domain.exception.UserNotFoundException;
import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.domain.repository.UserRepository;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import br.com.rafaellbarros.user.mapper.UserMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for user management operations
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Creates a new user
     *
     * @param request user creation data
     * @return created user response
     * @throws BusinessException if email already exists
     */
    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        log.debug("Creating user with email: {}", request.getEmail());
        validateEmailUniqueness(request.getEmail());

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    /**
     * Retrieves a user by ID
     *
     * @param id user identifier
     * @return user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID id) {
        log.debug("Retrieving user with ID: {}", id);
        User user = getUserEntity(id);
        return userMapper.toResponse(user);
    }

    /**
     * Retrieves all active users
     *
     * @return list of user responses
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Retrieving all users");
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    /**
     * Retrieves all users including inactive ones
     *
     * @return list of all user responses
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersWithInactive() {
        log.debug("Retrieving all users including inactive");
        List<User> users = userRepository.findAllWithInactive();
        return userMapper.toResponseList(users);
    }

    /**
     * Updates user information
     *
     * @param id user identifier
     * @param request user update data
     * @return updated user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO request) {
        log.debug("Updating user with ID: {}", id);
        User user = getUserEntity(id);

        // Check if email is being changed and validate uniqueness
        if (!user.getEmail().equals(request.getEmail())) {
            validateEmailUniqueness(request.getEmail());
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    /**
     * Deactivates a user (soft delete)
     *
     * @param id user identifier
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void deactivateUser(UUID id) {
        log.debug("Deactivating user with ID: {}", id);
        User user = getUserEntity(id);
        user.deactivate();
        userRepository.save(user);
        log.info("User deactivated successfully with ID: {}", id);
    }

    /**
     * Reactivates a previously deactivated user
     *
     * @param id user identifier
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void reactivateUser(UUID id) {
        log.debug("Reactivating user with ID: {}", id);
        User user = getUserEntity(id);
        user.reactivate();
        userRepository.save(user);
        log.info("User reactivated successfully with ID: {}", id);
    }

    /**
     * Permanently deletes a user (hard delete)
     *
     * @param id user identifier
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.debug("Deleting user with ID: {}", id);
        User user = getUserEntity(id);
        userRepository.delete(user);
        log.info("User deleted permanently with ID: {}", id);
    }

    /**
     * Retrieves user entity by ID
     *
     * @param id user identifier
     * @return user entity
     * @throws UserNotFoundException if user not found
     */
    @NonNull
    private User getUserEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Validates email uniqueness
     *
     * @param email email to validate
     * @throws BusinessException if email already exists
     */
    private void validateEmailUniqueness(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new BusinessException(
                            String.format("Email already registered: %s", email)
                    );
                });
    }
}