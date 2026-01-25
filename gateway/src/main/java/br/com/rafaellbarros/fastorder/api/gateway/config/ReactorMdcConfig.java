package br.com.rafaellbarros.fastorder.api.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ReactorMdcConfig {

    @PostConstruct
    public void setup() {
        Hooks.enableAutomaticContextPropagation();
    }
}
