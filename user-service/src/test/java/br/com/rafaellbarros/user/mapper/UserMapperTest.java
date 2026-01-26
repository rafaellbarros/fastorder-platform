package br.com.rafaellbarros.user.mapper;

import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper
 */
@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Nested
    @DisplayName("When mapping CreateUserRequestDTO to User")
    class ToEntityTests {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // Given
            CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .build();

            // When
            User user = userMapper.toEntity(request);

            // Then
            assertThat(user)
                    .isNotNull()
                    .satisfies(u -> {
                        assertThat(u.getId()).isNull();
                        assertThat(u.getName()).isEqualTo("John Doe");
                        assertThat(u.getEmail()).isEqualTo("john.doe@example.com");
                        assertThat(u.isActive()).isTrue();
                    });
        }

        @Test
        @DisplayName("Should set active to true by default")
        void shouldSetActiveToTrue() {
            // Given
            CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .build();

            // When
            User user = userMapper.toEntity(request);

            // Then
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("When mapping User to UserResponseDTO")
    class ToResponseTests {

        private User user;
        private final UUID userId = UUID.randomUUID();
        private final LocalDateTime now = LocalDateTime.now();

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .id(userId)
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .active(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        @Test
        @DisplayName("Should map all fields correctly for active user")
        void shouldMapAllFieldsForActiveUser() {
            // When
            UserResponseDTO response = userMapper.toResponse(user);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(dto -> {
                        assertThat(dto.getId()).isEqualTo(userId);
                        assertThat(dto.getName()).isEqualTo("John Doe");
                        assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
                        assertThat(dto.isActive()).isTrue();
                        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
                        assertThat(dto.getCreatedAt()).isEqualTo(now);
                        assertThat(dto.getUpdatedAt()).isEqualTo(now);
                    });
        }

        @Test
        @DisplayName("Should map status to INACTIVE when user is not active")
        void shouldMapStatusToInactive() {
            // Given
            user.setActive(false);

            // When
            UserResponseDTO response = userMapper.toResponse(user);

            // Then
            assertThat(response.getStatus()).isEqualTo("INACTIVE");
            assertThat(response.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("When mapping list of Users to list of UserResponseDTO")
    class ToResponseListTests {

        @Test
        @DisplayName("Should map list correctly")
        void shouldMapListCorrectly() {
            // Given
            List<User> users = List.of(
                    User.builder()
                            .id(UUID.randomUUID())
                            .name("User 1")
                            .email("user1@example.com")
                            .active(true)
                            .build(),
                    User.builder()
                            .id(UUID.randomUUID())
                            .name("User 2")
                            .email("user2@example.com")
                            .active(false)
                            .build()
            );

            // When
            List<UserResponseDTO> responseList = userMapper.toResponseList(users);

            // Then
            assertThat(responseList)
                    .hasSize(2)
                    .extracting(UserResponseDTO::getName)
                    .containsExactly("User 1", "User 2");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyList() {
            // When
            List<UserResponseDTO> responseList = userMapper.toResponseList(List.of());

            // Then
            assertThat(responseList).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when input is null")
        void shouldReturnEmptyListWhenNull() {
            // When
            List<UserResponseDTO> responseList = userMapper.toResponseList(null);

            // Then
            assertThat(responseList).isNull();
        }
    }

    @Nested
    @DisplayName("When updating User from UpdateUserRequestDTO")
    class UpdateEntityTests {

        private User existingUser;
        private final UUID userId = UUID.randomUUID();
        private final LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        @BeforeEach
        void setUp() {
            existingUser = User.builder()
                    .id(userId)
                    .name("Original Name")
                    .email("original@example.com")
                    .active(true)
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();
        }

        @Test
        @DisplayName("Should update only non-null fields")
        void shouldUpdateOnlyNonNullFields() {
            // Given
            UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                    .name("Updated Name")
                    .email(null) // Should not update email
                    .build();

            // When
            userMapper.updateEntity(request, existingUser);

            // Then
            assertThat(existingUser)
                    .satisfies(user -> {
                        assertThat(user.getId()).isEqualTo(userId);
                        assertThat(user.getName()).isEqualTo("Updated Name");
                        assertThat(user.getEmail()).isEqualTo("original@example.com");
                        assertThat(user.isActive()).isTrue();
                        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
                        assertThat(user.getUpdatedAt()).isEqualTo(createdAt); // Não deve atualizar timestamp no mapper
                    });
        }

        @Test
        @DisplayName("Should update email when provided")
        void shouldUpdateEmailWhenProvided() {
            // Given
            UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                    .email("updated@example.com")
                    .build();

            // When
            userMapper.updateEntity(request, existingUser);

            // Then
            assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
            // createdAt e updatedAt não devem mudar no mapper
            assertThat(existingUser.getUpdatedAt()).isEqualTo(createdAt);
        }
    }
}