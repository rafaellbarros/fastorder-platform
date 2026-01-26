package br.com.rafaellbarros.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User update request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User update request data")
public class UpdateUserRequestDTO {

    @Size(min = 2, max = 100, message = "{javax.validation.constraints.Size.message}")
    @Schema(description = "User full name", example = "John Doe Updated")
    private String name;

    @Email(message = "{javax.validation.constraints.Email.message}")
    @Schema(description = "User email address", example = "john.updated@example.com")
    private String email;

}