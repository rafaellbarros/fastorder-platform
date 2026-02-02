package br.com.rafaellbarros.user.security;

import br.com.rafaellbarros.user.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private ApiErrorResponseWriter writer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    private CustomAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CustomAccessDeniedHandler(writer);
    }

    @Test
    void handle_writesForbiddenApiError() throws IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/secure/resource");
        when(accessDeniedException.getMessage()).thenReturn("Access is denied");

        // Act
        handler.handle(request, response, accessDeniedException);

        // Assert
        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(writer).write(eq(response), errorCaptor.capture());

        ApiError error = errorCaptor.getValue();
        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), error.getStatus()),
            () -> assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), error.getError()),
            () -> assertEquals("Access denied", error.getMessage()),
            () -> assertEquals("/api/secure/resource", error.getPath()),
            () -> assertNotNull(error.getTimestamp()),
            () -> assertTrue(Instant.now().compareTo(error.getTimestamp()) >= 0)
        );
    }


    @Test
    void handle_withDifferentPaths() throws IOException {
        // Arrange
        String[] testPaths = {"/api/orders", "/admin/dashboard", "/users/profile"};

        for (String path : testPaths) {
            when(request.getRequestURI()).thenReturn(path);
            reset(writer); // Reseta o mock para cada iteração

            // Act
            handler.handle(request, response, accessDeniedException);

            // Assert
            ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
            verify(writer).write(eq(response), errorCaptor.capture());

            assertEquals(path, errorCaptor.getValue().getPath());
        }
    }

    @Test
    void handle_whenIOExceptionThrown_propagatesException() throws IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        doThrow(new IOException("Write error")).when(writer).write(any(), any());

        // Act & Assert
        assertThrows(IOException.class, () -> 
            handler.handle(request, response, accessDeniedException)
        );
    }

    @Test
    void constructor_injectsDependencies() {
        // Act & Assert
        assertNotNull(handler);
        // Verifica que o handler foi criado com o writer
        // Como writer é private, testamos indiretamente pelo comportamento
        assertDoesNotThrow(() -> handler.handle(request, response, accessDeniedException));
    }
}