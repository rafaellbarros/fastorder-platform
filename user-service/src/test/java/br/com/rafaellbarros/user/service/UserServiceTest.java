package br.com.rafaellbarros.user.service;

import br.com.rafaellbarros.user.domain.exception.BusinessException;
import br.com.rafaellbarros.user.domain.exception.UserNotFoundException;
import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.domain.repository.UserRepository;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import br.com.rafaellbarros.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService using BDD Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UUID userId;
    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john.doe@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .id(userId)
                .name("John Doe")
                .email("john.doe@example.com")
                .active(true)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("When creating a user")
    class CreateUserTests {

        private CreateUserRequestDTO createRequest;

        @BeforeEach
        void setUp() {
            createRequest = CreateUserRequestDTO.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .build();
        }

        @Test
        @DisplayName("Should create user successfully when email is unique")
        void shouldCreateUserSuccessfully() {
            // Given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(userMapper.toEntity(createRequest)).willReturn(user);
            given(userRepository.save(user)).willReturn(user); // <-- IMPORTANTE: retorna o usuário
            given(userMapper.toResponse(user)).willReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.createUser(createRequest);

            // Then
            assertThat(result).isEqualTo(userResponseDTO);
            then(userRepository).should().findByEmail("john.doe@example.com");
            then(userRepository).should().save(user);
            then(userMapper).should().toResponse(user);
        }

        @Test
        @DisplayName("Should throw BusinessException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Email already registered: john.doe@example.com");

            then(userRepository).should(never()).save(any());
        }

        @Nested
        @DisplayName("When validating email uniqueness")
        class EmailValidationTests {

            @Test
            @DisplayName("Should pass validation when email does not exist")
            void shouldPassValidationWhenEmailDoesNotExist() {
                // Given
                CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                        .name("Test")
                        .email("new@example.com")
                        .build();

                given(userRepository.findByEmail("new@example.com")).willReturn(Optional.empty());
                given(userMapper.toEntity(request)).willReturn(user);
                given(userRepository.save(user)).willReturn(user); // <-- ADICIONE ESTA LINHA
                given(userMapper.toResponse(user)).willReturn(userResponseDTO);

                // When
                UserResponseDTO result = userService.createUser(request);

                // Then
                assertThat(result).isEqualTo(userResponseDTO);
            }

            @Test
            @DisplayName("Should fail validation when email exists")
            void shouldFailValidationWhenEmailExists() {
                // Given
                given(userRepository.findByEmail("existing@example.com")).willReturn(Optional.of(user));

                // When & Then
                assertThatThrownBy(() -> userService.createUser(CreateUserRequestDTO.builder()
                        .name("Test")
                        .email("existing@example.com")
                        .build()))
                        .isInstanceOf(BusinessException.class);

                then(userRepository).should(never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("When updating a user")
    class UpdateUserTests {

        private UpdateUserRequestDTO updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = UpdateUserRequestDTO.builder()
                    .name("Updated Name")
                    .email("updated@example.com")
                    .build();
        }

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findByEmail("updated@example.com")).willReturn(Optional.empty());

            // Configurar o mock do updateEntity para realmente atualizar o usuário
            doAnswer(invocation -> {
                UpdateUserRequestDTO request = invocation.getArgument(0);
                User userToUpdate = invocation.getArgument(1);

                // Simular a lógica do mapper
                if (request.getName() != null) {
                    userToUpdate.setName(request.getName());
                }
                if (request.getEmail() != null) {
                    userToUpdate.setEmail(request.getEmail());
                }
                return null;
            }).when(userMapper).updateEntity(any(UpdateUserRequestDTO.class), any(User.class));

            // Quando salvar, retorna o mesmo usuário (já atualizado)
            given(userRepository.save(any(User.class))).willAnswer(invocation ->
                    invocation.getArgument(0));

            UserResponseDTO updatedResponse = UserResponseDTO.builder()
                    .id(userId)
                    .name("Updated Name")
                    .email("updated@example.com")
                    .build();
            given(userMapper.toResponse(any(User.class))).willReturn(updatedResponse);

            // When
            UserResponseDTO result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result).isEqualTo(updatedResponse);

            // Verificar que o usuário foi atualizado
            then(userMapper).should().updateEntity(eq(updateRequest), eq(user));
            then(userRepository).should().save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getName()).isEqualTo("Updated Name");
            assertThat(savedUser.getEmail()).isEqualTo("updated@example.com");
        }

        @Nested
        @DisplayName("When getting all users")
        class GetAllUsersTests {

            @Test
            @DisplayName("Should return all active users")
            void shouldReturnAllActiveUsers() {
                // Given
                List<User> users = List.of(user);
                List<UserResponseDTO> expectedResponse = List.of(userResponseDTO);

                given(userRepository.findAll()).willReturn(users);
                given(userMapper.toResponseList(users)).willReturn(expectedResponse);

                // When
                List<UserResponseDTO> result = userService.getAllUsers();

                // Then
                assertThat(result).isEqualTo(expectedResponse);
                assertThat(result).hasSize(1);
                then(userRepository).should().findAll();
            }

            @Test
            @DisplayName("Should return all users including inactive")
            void shouldReturnAllUsersWithInactive() {
                // Given
                User inactiveUser = User.builder()
                        .id(UUID.randomUUID())
                        .name("Inactive User")
                        .email("inactive@example.com")
                        .active(false)
                        .build();

                List<User> allUsers = List.of(user, inactiveUser);

                UserResponseDTO inactiveResponse = UserResponseDTO.builder()
                        .id(inactiveUser.getId())
                        .name("Inactive User")
                        .email("inactive@example.com")
                        .active(false)
                        .status("INACTIVE")
                        .build();

                List<UserResponseDTO> expectedResponse = List.of(userResponseDTO, inactiveResponse);

                given(userRepository.findAllWithInactive()).willReturn(allUsers);
                given(userMapper.toResponseList(allUsers)).willReturn(expectedResponse);

                // When
                List<UserResponseDTO> result = userService.getAllUsersWithInactive();

                // Then
                assertThat(result).isEqualTo(expectedResponse);
                assertThat(result).hasSize(2);
                then(userRepository).should().findAllWithInactive();
            }

            @Test
            @DisplayName("Should return empty list when no users exist")
            void shouldReturnEmptyListWhenNoUsers() {
                // Given
                given(userRepository.findAll()).willReturn(List.of());
                given(userMapper.toResponseList(List.of())).willReturn(List.of());

                // When
                List<UserResponseDTO> result = userService.getAllUsers();

                // Then
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("Should return empty list when no users exist including inactive")
            void shouldReturnEmptyListWhenNoUsersWithInactive() {
                // Given
                given(userRepository.findAllWithInactive()).willReturn(List.of());
                given(userMapper.toResponseList(List.of())).willReturn(List.of());

                // When
                List<UserResponseDTO> result = userService.getAllUsersWithInactive();

                // Then
                assertThat(result).isEmpty();
            }
        }

        @Test
        @DisplayName("Should not validate email uniqueness when email unchanged")
        void shouldNotValidateEmailWhenUnchanged() {
            // Given
            updateRequest.setEmail("john.doe@example.com"); // Mesmo email

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // Configurar o mock do updateEntity
            doNothing().when(userMapper).updateEntity(any(UpdateUserRequestDTO.class), any(User.class));

            given(userRepository.save(any(User.class))).willReturn(user);
            given(userMapper.toResponse(user)).willReturn(userResponseDTO);

            // When
            userService.updateUser(userId, updateRequest);

            // Then
            // Não deve validar email quando é o mesmo
            then(userRepository).should(never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should validate email uniqueness when email changed")
        void shouldValidateEmailWhenChanged() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findByEmail("updated@example.com")).willReturn(Optional.empty());

            // Configurar o mock do updateEntity
            doNothing().when(userMapper).updateEntity(any(UpdateUserRequestDTO.class), any(User.class));

            given(userRepository.save(any(User.class))).willReturn(user);
            given(userMapper.toResponse(user)).willReturn(userResponseDTO);

            // When
            userService.updateUser(userId, updateRequest);

            // Then
            // Deve validar email quando mudou
            then(userRepository).should().findByEmail("updated@example.com");
        }
    }

    @Nested
    @DisplayName("When getting user by ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toResponse(user)).willReturn(userResponseDTO);

            // When
            UserResponseDTO result = userService.getUserById(userId);

            // Then
            assertThat(result).isEqualTo(userResponseDTO);
            then(userRepository).should().findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(userId.toString());
        }
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        // Given
        List<User> users = List.of(user);
        List<UserResponseDTO> expectedResponse = List.of(userResponseDTO);

        given(userRepository.findAll()).willReturn(users);
        given(userMapper.toResponseList(users)).willReturn(expectedResponse);

        // When
        List<UserResponseDTO> result = userService.getAllUsers();

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUser() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        userService.deactivateUser(userId);

        // Then
        then(userRepository).should().save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should reactivate user successfully")
    void shouldReactivateUser() {
        // Given
        user.setActive(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        userService.reactivateUser(userId);

        // Then
        then(userRepository).should().save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUser() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        userService.deleteUser(userId);

        // Then
        then(userRepository).should().delete(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found for delete")
    void shouldThrowExceptionWhenUserNotFoundForDelete() {
        // Given
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should(never()).delete(any());
    }
}