package br.com.rafaellbarros.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .baseUrl("http://payment-service:8083")
                .build();
    }
}