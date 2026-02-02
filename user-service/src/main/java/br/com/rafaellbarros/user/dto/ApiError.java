package br.com.rafaellbarros.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API error response")
public class ApiError {

    @Schema(description = "Timestamp when the error occurred", example = "2024-01-26T01:10:40.605201355Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "HTTP status reason", example = "Bad Request")
    private String error;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "Error message developer details", example = "Field 'email' must not be null")
    private String messageDeveloper;

    @Schema(description = "API path where the error occurred", example = "/api/v1/users")
    private String path;

    @Schema(description = "Detailed validation errors")
    private List<ValidationError> validationErrors;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error details")
    public static class ValidationError {

        @Schema(description = "Field that failed validation", example = "email")
        private String field;

        @Schema(description = "Rejected value", example = "null")
        private Object rejectedValue;

        @Schema(description = "Error message", example = "Email is required")
        private String message;

        @Schema(description = "Validation constraint", example = "NotBlank")
        private String constraint;
    }
}
