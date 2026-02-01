package br.com.rafaellbarros.user.domain.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FriendlyFieldErrorResolverTest {

    private MessageSource messageSource;
    private FriendlyFieldErrorResolver resolver;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        resolver = new FriendlyFieldErrorResolver(messageSource);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @Test
    void resolveFieldName_returnsFriendlyName_whenMessageExists() {
        when(messageSource.getMessage(eq("field.username"), any(), any(Locale.class)))
                .thenReturn("Username");

        String result = resolver.resolveFieldName("username");

        assertEquals("Username", result);
    }

    @Test
    void resolveFieldName_returnsFieldName_whenMessageNotFound() {
        when(messageSource.getMessage(eq("field.email"), any(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("field.email"));

        String result = resolver.resolveFieldName("email");

        assertEquals("email", result);
    }
}