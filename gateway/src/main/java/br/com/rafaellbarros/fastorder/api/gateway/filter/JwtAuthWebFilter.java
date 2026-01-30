package br.com.rafaellbarros.fastorder.api.gateway.filter;

import br.com.rafaellbarros.fastorder.api.gateway.security.CachedJwtAuthenticationManager;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthWebFilter extends AuthenticationWebFilter {

    public JwtAuthWebFilter(CachedJwtAuthenticationManager authManager) {
        super(authManager);

        setServerAuthenticationConverter(exchange -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return Mono.just(new BearerTokenAuthenticationToken(token));
            }
            return Mono.empty();
        });
    }
}
