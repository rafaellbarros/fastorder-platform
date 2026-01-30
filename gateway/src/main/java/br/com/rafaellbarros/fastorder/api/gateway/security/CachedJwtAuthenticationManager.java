package br.com.rafaellbarros.fastorder.api.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class CachedJwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtAuthCacheService cacheService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String token = (String) authentication.getCredentials();

        return Mono.fromCallable(() -> cacheService.decodeAndConvertBlocking(token))
                .subscribeOn(Schedulers.boundedElastic())
                .cast(Authentication.class);
    }
}
