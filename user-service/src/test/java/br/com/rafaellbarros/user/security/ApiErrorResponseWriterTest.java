package br.com.rafaellbarros.user.security;

import br.com.rafaellbarros.user.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiErrorResponseWriterTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    private ApiErrorResponseWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ApiErrorResponseWriter(mapper);
    }

    @Test
    void write_setsResponsePropertiesAndWritesError() throws Exception {
        // Arrange
        ApiError error = ApiError.builder()
                .status(400)
                .message("Bad request")
                .build();

        when(response.getWriter()).thenReturn(printWriter);

        // Act
        writer.write(response, error);

        // Assert
        verify(response).setStatus(400);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(mapper).writeValue(printWriter, error);
        verify(printWriter).flush();
    }

    @Test
    void write_withDifferentStatus() throws Exception {
        // Arrange
        ApiError error = ApiError.builder()
                .status(404)
                .message("Not found")
                .build();

        when(response.getWriter()).thenReturn(printWriter);

        // Act
        writer.write(response, error);

        // Assert
        verify(response).setStatus(404);
        verify(mapper).writeValue(printWriter, error);
    }
}