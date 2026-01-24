package br.com.rafaellbarros.observability.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe que aplica e gerencia as configurações de observabilidade
 */
public class ObservabilityConfigurer implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ObservabilityConfigurer.class);
    
    private final Environment environment;
    private final ObservabilityProperties properties;
    
    // Cache de propriedades calculadas
    private Map<String, String> computedProperties;
    
    public ObservabilityConfigurer(Environment environment, 
                                  ObservabilityProperties properties) {
        this.environment = environment;
        this.properties = properties;
        this.computedProperties = new HashMap<>();
    }
    
    /**
     * Método chamado quando a aplicação está pronta
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initializeObservability();
        logConfiguration();
    }
    
    /**
     * Inicializa as configurações de observabilidade
     */
    private void initializeObservability() {
        String appName = environment.getProperty("spring.application.name", "unknown-service");
        String[] activeProfiles = environment.getActiveProfiles();
        
        // Calcula propriedades baseadas no ambiente
        computedProperties.put("app.name", appName);
        computedProperties.put("active.profiles", String.join(",", activeProfiles));
        computedProperties.put("tracing.enabled", String.valueOf(properties.isEnabled()));
        computedProperties.put("tracing.sampling.rate", String.valueOf(properties.getSamplingRate()));
        
        // Configura logging inicial
        MDC.put("service", appName);
        MDC.put("profiles", String.join(",", activeProfiles));
        
        log.info("Observabilidade inicializada para {}", appName);
    }
    
    /**
     * Loga a configuração atual
     */
    private void logConfiguration() {
        log.info("""
            📋 CONFIGURAÇÃO DE OBSERVABILIDADE
            =================================
            Serviço: {}
            Profiles: {}
            Tracing Habilitado: {}
            Sampling Rate: {}
            Config Source: {}
            =================================
            """,
            computedProperties.get("app.name"),
            computedProperties.get("active.profiles"),
            computedProperties.get("tracing.enabled"),
            computedProperties.get("tracing.sampling.rate"),
            properties.getConfigSource()
        );
    }
    
    /**
     * Métodos utilitários para uso em outros componentes
     */
    public boolean isTracingEnabled() {
        return properties.isEnabled();
    }
    
    public double getSamplingRate() {
        return properties.getSamplingRate();
    }
    
    public String getServiceName() {
        return computedProperties.get("app.name");
    }
    
    public String getConfigSource() {
        return properties.getConfigSource();
    }
    
    /**
     * Gera um correlation ID para requisições
     */
    public String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8) + 
               "-" + System.currentTimeMillis();
    }
}