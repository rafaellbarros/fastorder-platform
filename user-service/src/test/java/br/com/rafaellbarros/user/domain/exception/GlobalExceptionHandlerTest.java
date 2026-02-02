package br.com.rafaellbarros.user.domain.exception;

import br.com.rafaellbarros.user.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private FriendlyFieldErrorResolver fieldResolver;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        fieldResolver = mock(FriendlyFieldErrorResolver.class);
        handler = new GlobalExceptionHandler(fieldResolver);
    }

    @Test
    void handleValidationException_returnsBadRequestFieldErros() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "field", "invalid"));
        when(fieldResolver.resolveFieldName("field")).thenReturn("Field");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        var request = mock(org.springframework.web.context.request.ServletWebRequest.class);
        when(request.getRequest()).thenReturn(mock(HttpServletRequest.class));
        when(request.getRequest().getRequestURI()).thenReturn("/test");

        ResponseEntity<ApiError> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
    }

    @Test
    void handleValidationException_returnsBadRequestGlobalErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new ObjectError("object", "global error"));
        when(fieldResolver.resolveFieldName("field")).thenReturn("Field");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        var request = mock(org.springframework.web.context.request.ServletWebRequest.class);
        when(request.getRequest()).thenReturn(mock(HttpServletRequest.class));
        when(request.getRequest().getRequestURI()).thenReturn("/test");

        ResponseEntity<ApiError> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
    }

    @Test
    void handleBusinessException_returnsConflict() {
        BusinessException ex = new BusinessException("Business error");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/business");

        ResponseEntity<ApiError> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Business error", response.getBody().getMessage());
    }

    @Test
    void handleUserNotFoundException_returnsNotFound() {
        UserNotFoundException ex = new UserNotFoundException(UUID.fromString("bfc8a0c3-2630-4b9a-beca-fede1b4a3f87"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/user");

        ResponseEntity<ApiError> response = handler.handleUserNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: bfc8a0c3-2630-4b9a-beca-fede1b4a3f87", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException_returnsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/json");

        ResponseEntity<ApiError> response = handler.handleHttpMessageNotReadableException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Invalid"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_returnsBadRequest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);
        when(ex.getMessage()).thenReturn("Type mismatch");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/type");

        ResponseEntity<ApiError> response = handler.handleMethodArgumentTypeMismatchException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Parameter 'id'"));
    }

    @Test
    void handleAccessDeniedException_returnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/forbidden");

        ResponseEntity<ApiError> response = handler.handleAccessDeniedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Unexpected");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/error");

        ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException_setsMessageFromCause_whenCauseExists() {
        Throwable cause = new RuntimeException("Root cause message");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", cause);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/json");

        ResponseEntity<ApiError> response = handler.handleHttpMessageNotReadableException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Root cause message", response.getBody().getMessage());
        assertEquals("/json", response.getBody().getPath());
    }

}