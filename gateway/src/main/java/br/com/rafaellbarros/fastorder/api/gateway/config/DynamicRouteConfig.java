package br.com.rafaellbarros.fastorder.api.gateway.config;

import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicRouteConfig {

    @Bean
    public RouteDefinitionRepository routeDefinitionRepository() {
        return new InMemoryRouteDefinitionRepository();
    }
}