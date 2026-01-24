package br.com.rafaellbarros.observability.config;

public class ObservabilityConstants {
    
    // Profiles
    public static final String PROFILE_LOCAL = "local";
    public static final String PROFILE_DEV = "dev";
    public static final String PROFILE_STAGE = "stage";
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_CLOUD = "prod";
    
    // Property keys
    public static final String PROP_TRACING_SAMPLING = "management.tracing.sampling.probability";
    public static final String PROP_LOGGING_PATTERN = "logging.pattern.level";
    
    // Log messages
    public static final String LOG_TRACE_PREFIX = "[traceId=%s, spanId=%s]";
    public static final String LOG_APP_PREFIX = "[app=%s]";
    
    // Config sources
    public static final String CONFIG_SOURCE_LOCAL = "local";
    public static final String CONFIG_SOURCE_SERVER = "config-server";
    public static final String CONFIG_SOURCE_KUBERNETES = "kubernetes";
}