package br.com.rafaellbarros.user.controller;

import br.com.rafaellbarros.user.config.TestSecurityConfig;
import br.com.rafaellbarros.user.domain.exception.BusinessException;
import br.com.rafaellbarros.user.domain.exception.FriendlyFieldErrorResolver;
import br.com.rafaellbarros.user.domain.exception.UserNotFoundException;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.PageResponseDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import br.com.rafaellbarros.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, FriendlyFieldErrorResolver.class})
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FriendlyFieldErrorResolver fieldResolver;

    private UUID userId;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {

        given(fieldResolver.resolveFieldName(anyString()))
                .willAnswer(inv -> inv.getArgument(0));

        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
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

    @Test
    @DisplayName("POST /api/v1/users - Should create user and return 201 with location header")
    void shouldCreateUserAndReturn201() throws Exception {
        // Given
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        given(userService.createUser(any(CreateUserRequestDTO.class)))
                .willReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/v1/users/" + userId)))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).createUser(any(CreateUserRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should return user when found")
    void shouldReturnUserWhenFound() throws Exception {
        // Given
        given(userService.getUserById(userId)).willReturn(userResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/v1/users - Should return all users")
    void shouldReturnAllUsers() throws Exception {
        // Given
        List<UserResponseDTO> users = List.of(userResponseDTO);
        given(userService.getAllUsers()).willReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/v1/users/all - Should return all users including inactive")
    void shouldReturnAllUsersIncludingInactive() throws Exception {
        List<UserResponseDTO> users = List.of(userResponseDTO);

        given(userService.getAllUsersWithInactive()).willReturn(users);

        mockMvc.perform(get("/api/v1/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()));

        verify(userService).getAllUsersWithInactive();
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserResponseDTO updatedResponse = UserResponseDTO.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        given(userService.updateUser(eq(userId), any(UpdateUserRequestDTO.class)))
                .willReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).updateUser(eq(userId), any(UpdateUserRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Should delete user and return 204")
    void shouldDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", userId.toString()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 400 when request is invalid")
    void shouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        CreateUserRequestDTO invalidRequest = CreateUserRequestDTO.builder()
                .name("") // Invalid: empty name
                .email("invalid-email") // Invalid email format
                .build();


        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field").exists())
                .andExpect(jsonPath("$.validationErrors[*].message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        given(userService.getUserById(userId))
                .willThrow(new UserNotFoundException(userId));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // Given
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .name("John Doe")
                .email("existing@example.com")
                .build();

        given(userService.createUser(any(CreateUserRequestDTO.class)))
                .willThrow(new BusinessException("Email already registered: existing@example.com"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already registered")));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Should return 400 when update request is invalid")
    void shouldReturn400WhenInvalidUpdateRequest() throws Exception {
        // Given
        UpdateUserRequestDTO invalidRequest = UpdateUserRequestDTO.builder()
                .name("A") // Too short
                .email("invalid-email") // Invalid email
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should return 400 when ID is invalid")
    void shouldReturn400WhenInvalidIdFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/deactivate - Should return 204")
    void shouldDeactivateUser() throws Exception {

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deactivateUser(userId);
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/reactivate - Should return 204")
    void shouldReactivateUser() throws Exception {

        mockMvc.perform(patch("/api/v1/users/{id}/reactivate", userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).reactivateUser(userId);
    }




    @Test
    @DisplayName("GET /api/v1/users/all - Should return 500 on unexpected error")
    void shouldReturn500WhenGetAllWithInactiveFails() throws Exception {

        given(userService.getAllUsersWithInactive())
                .willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/users/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/users/paged - Should use default page=0 size=10")
    void shouldUseDefaultPagination() throws Exception {

        PageResponseDTO<UserResponseDTO> pageResponse =
                new PageResponseDTO<>(List.of(userResponseDTO), 0, 10, 1, 1, true);

        given(userService.getAllUsers(0, 10)).willReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users/paged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        then(userService).should().getAllUsers(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/users/paged - Should use provided page and size")
    void shouldUseProvidedPagination() throws Exception {

        PageResponseDTO<UserResponseDTO> pageResponse =
                new PageResponseDTO<>(List.of(userResponseDTO), 2, 5, 20, 4, false);

        given(userService.getAllUsers(2, 5)).willReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users/paged")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.last").value(false));

        then(userService).should().getAllUsers(2, 5);
    }


    @Test
    @DisplayName("GET /api/v1/users/paged - Should return empty page")
    void shouldReturnEmptyPage() throws Exception {

        PageResponseDTO<UserResponseDTO> emptyPage =
                new PageResponseDTO<>(List.of(), 0, 10, 0, 0, true);

        given(userService.getAllUsers(0, 10)).willReturn(emptyPage);

        mockMvc.perform(get("/api/v1/users/paged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/users/paged - Should return 400 when page is invalid")
    void shouldReturn400WhenPageInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/users/paged")
                        .param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/users/paged - Should return 500 on unexpected error")
    void shouldReturn500WhenPagedFails() throws Exception {

        given(userService.getAllUsers(anyInt(), anyInt()))
                .willThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/v1/users/paged"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }
}
