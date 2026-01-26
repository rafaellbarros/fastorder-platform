package br.com.rafaellbarros.user.domain.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendlyFieldErrorResolver {

    private final MessageSource messageSource;

    public String resolveFieldName(String fieldName) {
        try {
            return messageSource.getMessage("field." + fieldName, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException ex) {
            return fieldName; // fallback
        }
    }
}