package br.com.rafaellbarros.user.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private ApiErrorResponseWriter writer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void shouldHandleRequestWithoutExceptionMessage() throws IOException {
        // Given
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint(writer);
        when(request.getRequestURI()).thenReturn("/api/test");
        AuthenticationException exception = mock(AuthenticationException.class);

        // When
        entryPoint.commence(request, response, exception);

        // Then
        verify(writer).write(any(), any());
    }
}