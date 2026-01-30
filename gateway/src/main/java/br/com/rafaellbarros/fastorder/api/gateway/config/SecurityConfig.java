package br.com.rafaellbarros.fastorder.api.gateway.config;

import br.com.rafaellbarros.fastorder.api.gateway.filter.JwtAuthWebFilter;
import br.com.rafaellbarros.fastorder.api.gateway.security.SecurityExceptionHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthWebFilter jwtAuthWebFilter;
    private final SecurityExceptionHandlers exceptionHandlers;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(exceptionHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(exceptionHandlers.accessDeniedHandler())
                )
                .authorizeExchange(ex -> ex
                        .matchers(new PathPatternParserServerWebExchangeMatcher("/actuator/**")).permitAll()
                        .pathMatchers("/*/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}





