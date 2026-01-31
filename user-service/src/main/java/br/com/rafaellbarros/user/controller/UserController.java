package br.com.rafaellbarros.user.controller;

import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.PageResponseDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import br.com.rafaellbarros.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user management operations
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user
     */
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user and returns the created resource with Location header"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully. Returns the created user and Location header",
                    headers = @io.swagger.v3.oas.annotations.headers.Header(
                            name = "Location",
                            description = "URL of the created user resource",
                            example = "/api/v1/users/123e4567-e89b-12d3-a456-426614174000"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists in the system"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        log.debug("Creating user with email: {}", request.getEmail());
        UserResponseDTO response = userService.createUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        log.info("User created successfully with ID: {}", response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /**
     * Retrieves a user by ID
     */
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user details by their unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") UUID id) {
        log.debug("Retrieving user with ID: {}", id);
        UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all active users
     */
    @Operation(
            summary = "Get all active users",
            description = "Retrieves a list of all currently active users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of active users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.debug("Retrieving all active users");
        List<UserResponseDTO> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all users pageable
     */
    @Operation(
            summary = "Get all users pageable",
            description = "Retrieves a list of all currently active users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of active users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(path = "/paged", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Retrieving users pageable — page={}, size={}", page, size);

        PageResponseDTO<UserResponseDTO> response = userService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all users including inactive
     */
    @Operation(
            summary = "Get all users including inactive",
            description = "Retrieves a list of all users, including deactivated ones. Requires appropriate permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of all users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(
            value = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserResponseDTO>> getAllUsersWithInactive() {
        log.debug("Retrieving all users including inactive");
        List<UserResponseDTO> response = userService.getAllUsersWithInactive();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates user information
     */
    @Operation(
            summary = "Update user",
            description = "Updates user information. If email is changed, validates uniqueness."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or email already exists"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        log.debug("Updating user with ID: {}", id);
        UserResponseDTO response = userService.updateUser(id, request);
        log.info("User updated successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivates a user (soft delete)
     */
    @Operation(
            summary = "Deactivate user",
            description = "Deactivates a user (soft delete). User data is preserved but marked as inactive."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(
            value = "/{id}/deactivate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        log.debug("Deactivating user with ID: {}", id);
        userService.deactivateUser(id);
        log.info("User deactivated successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a user
     */
    @Operation(
            summary = "Reactivate user",
            description = "Reactivates a previously deactivated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User reactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(
            value = "/{id}/reactivate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> reactivateUser(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        log.debug("Reactivating user with ID: {}", id);
        userService.reactivateUser(id);
        log.info("User reactivated successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
