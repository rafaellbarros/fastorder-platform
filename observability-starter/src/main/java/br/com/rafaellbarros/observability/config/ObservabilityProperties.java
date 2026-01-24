package br.com.rafaellbarros.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "observability")
@Validated
public class ObservabilityProperties {

    private boolean enabled = true;

    @Min(0)
    @Max(1)
    private double samplingRate = 1.0; // 100% local, 0.1 em produção

    private String configSource = "local"; // local, config-server, kubernetes

    private Logging logging = new Logging();
    private Metrics metrics = new Metrics();
    private Tracing tracing = new Tracing();

    // Getters e Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getSamplingRate() { return samplingRate; }
    public void setSamplingRate(double samplingRate) { this.samplingRate = samplingRate; }

    public String getConfigSource() { return configSource; }
    public void setConfigSource(String configSource) { this.configSource = configSource; }

    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }

    public Metrics getMetrics() { return metrics; }
    public void setMetrics(Metrics metrics) { this.metrics = metrics; }

    public Tracing getTracing() { return tracing; }
    public void setTracing(Tracing tracing) { this.tracing = tracing; }

    // Classes internas para agrupamento
    public static class Logging {
        private boolean structured = false;
        private String pattern = "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]";

        public boolean isStructured() { return structured; }
        public void setStructured(boolean structured) { this.structured = structured; }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
    }

    public static class Metrics {
        private boolean enabled = true;
        private String[] include = {"http.server.requests", "jvm", "system"};

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String[] getInclude() { return include; }
        public void setInclude(String[] include) { this.include = include; }
    }

    public static class Tracing {
        private boolean enabled = true;
        private String[] baggageFields = {"userId", "tenantId", "correlationId"};

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String[] getBaggageFields() { return baggageFields; }
        public void setBaggageFields(String[] baggageFields) { this.baggageFields = baggageFields; }
    }
}