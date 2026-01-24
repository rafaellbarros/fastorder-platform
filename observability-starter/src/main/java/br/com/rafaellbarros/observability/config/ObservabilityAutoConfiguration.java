package br.com.rafaellbarros.observability.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
public class ObservabilityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityAutoConfiguration.class);

    /**
     * Carrega as configurações padrão do arquivo YAML
     * Isso garante que as propriedades estejam disponíveis mesmo sem import explícito
     */
    @Bean
    static PropertySource<?> observabilityDefaults() throws IOException {
        var loader = new YamlPropertiesFactoryBean();
        loader.setResources(new ClassPathResource("observability-defaults.yml"));
        var properties = loader.getObject();

        if (properties == null || properties.isEmpty()) {
            log.warn("⚠️  Arquivo observability-defaults.yml não encontrado ou vazio");
            properties = new java.util.Properties();
        }

        log.info("✅ Configurações padrão de observabilidade carregadas ({} propriedades)",
                properties.size());

        return new org.springframework.core.env.PropertiesPropertySource(
                "observabilityDefaults",
                properties
        );
    }

    /**
     * Configurador principal que aplica as propriedades
     */
    @Bean
    public ObservabilityConfigurer observabilityConfigurer(
            Environment env,
            ObservabilityProperties properties) {

        String appName = env.getProperty("spring.application.name", "unknown-service");
        String activeProfiles = String.join(",", env.getActiveProfiles());

        log.info("""
            🚀 Observabilidade Configurada
            📦 Serviço: {}
            📊 Profiles Ativos: {}
            🔍 Sampling Rate: {}
            📍 Config Source: {}
            """,
                appName,
                activeProfiles.isEmpty() ? "default" : activeProfiles,
                properties.getSamplingRate(),
                properties.getConfigSource()
        );

        return new ObservabilityConfigurer(env, properties);
    }
}