package br.com.rafaellbarros.fastorder.api.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthCacheService {

    private final ReactiveJwtDecoder jwtDecoder;
    private final KeycloakJwtAuthenticationConverter converter;

    @Cacheable(value = "auth:jwt", key = "#token")
    public AbstractAuthenticationToken decodeAndConvertBlocking(String token) {
        Jwt jwt = jwtDecoder.decode(token).block();
        return converter.convert(jwt).block();
    }
}