package br.com.rafaellbarros.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for user response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User response payload")
public class UserResponseDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Full name of the user",
            example = "John Michael Doe"
    )
    private String name;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com"
    )
    private String email;


    @Schema(
            description = "User account status",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "PENDING"}
    )
    private String status;

    @JsonProperty("isActive")
    @Schema(
            description = "Indicates if the user account is active",
            example = "true"
    )
    private boolean active;


    @Schema(
            description = "Date and time when the user was created",
            example = "2024-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the user was last updated",
            example = "2024-01-20T15:45:30",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;



}