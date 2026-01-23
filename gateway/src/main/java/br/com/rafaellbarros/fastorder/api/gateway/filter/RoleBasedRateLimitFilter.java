package br.com.rafaellbarros.fastorder.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RoleBasedRateLimitFilter implements GlobalFilter, Ordered {

    private static final int USER_LIMIT = 100;

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {

                    String role = extractRole(auth);

                    // ADMIN não sofre rate limit
                    if ("ROLE_ADMIN".equals(role)) {
                        return chain.filter(exchange);
                    }

                    // Aplica rate limit para demais roles
                    counters.putIfAbsent(role, new AtomicInteger(0));

                    int requests = counters.get(role).incrementAndGet();

                    if (requests > USER_LIMIT) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                })
                // Se não houver Authentication (request sem token)
                .switchIfEmpty(handleAnonymous(exchange));
    }

    private String extractRole(Authentication auth) {

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ANON");
        }

        return "ANON";
    }

    private Mono<Void> handleAnonymous(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
