package io.lyven.core.config;

import io.lyven.core.di.Container;
import io.lyven.core.routing.Router;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Main configuration class for Lyven framework
 * Similar to Angular's AppConfig but for backend
 */
public class LyvenConfig {

    private final Properties properties;
    private final Container container;
    private final Router router;
    private final ObjectMapper objectMapper;

    // Default configuration values
    private int serverPort = 8080;
    private String serverHost = "localhost";
    private String contextPath = "";
    private boolean enableCors = true;
    private boolean enableDevMode = false;
    private String logLevel = "INFO";

    public LyvenConfig() {
        this.properties = new Properties();
        this.container = new Container();
        this.objectMapper = createDefaultObjectMapper();
        this.router = new Router(container);

        loadConfiguration();
    }

    /**
     * Load configuration from various sources
     */
    private void loadConfiguration() {
        // Load from properties file
        properties.loadFromFile("lyven.properties");

        // Load from environment variables
        properties.loadFromEnvironment("LYVEN_");

        // Apply loaded properties
        applyProperties();
    }

    /**
     * Apply loaded properties to configuration
     */
    private void applyProperties() {
        this.serverPort = properties.getInt("server.port", serverPort);
        this.serverHost = properties.getString("server.host", serverHost);
        this.contextPath = properties.getString("server.context-path", contextPath);
        this.enableCors = properties.getBoolean("server.cors.enabled", enableCors);
        this.enableDevMode = properties.getBoolean("lyven.dev-mode", enableDevMode);
        this.logLevel = properties.getString("logging.level", logLevel);
    }

    /**
     * Create default ObjectMapper with Lyven-specific configuration
     */
    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure for Java Records support
        mapper.findAndRegisterModules();

        // Configure JSON handling
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }

    /**
     * Register a component in the container
     */
    public <T> LyvenConfig register(Class<T> componentClass) {
        container.register(componentClass);
        return this;
    }

    /**
     * Register with interface binding
     */
    public <T> LyvenConfig register(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        container.register(interfaceClass, implementationClass);
        return this;
    }

    /**
     * Configure server port
     */
    public LyvenConfig port(int port) {
        this.serverPort = port;
        return this;
    }

    /**
     * Configure server host
     */
    public LyvenConfig host(String host) {
        this.serverHost = host;
        return this;
    }

    /**
     * Enable/disable CORS
     */
    public LyvenConfig cors(boolean enabled) {
        this.enableCors = enabled;
        return this;
    }

    /**
     * Enable/disable development mode
     */
    public LyvenConfig devMode(boolean enabled) {
        this.enableDevMode = enabled;
        return this;
    }

    /**
     * Set log level
     */
    public LyvenConfig logLevel(String level) {
        this.logLevel = level;
        return this;
    }

    /**
     * Get configuration summary
     */
    public void printConfiguration() {
        System.out.println("🔧 Lyven Configuration:");
        System.out.println("  Server: http://" + serverHost + ":" + serverPort + contextPath);
        System.out.println("  CORS: " + (enableCors ? "enabled" : "disabled"));
        System.out.println("  Dev Mode: " + (enableDevMode ? "enabled" : "disabled"));
        System.out.println("  Log Level: " + logLevel);
        System.out.println("  Registered components: " + container.getServiceRegistry().getRegistrationCount());
    }

    // Getters
    public int getServerPort() { return serverPort; }
    public String getServerHost() { return serverHost; }
    public String getContextPath() { return contextPath; }
    public boolean isCorsEnabled() { return enableCors; }
    public boolean isDevModeEnabled() { return enableDevMode; }
    public String getLogLevel() { return logLevel; }
    public Container getContainer() { return container; }
    public Router getRouter() { return router; }
    public ObjectMapper getObjectMapper() { return objectMapper; }
    public Properties getProperties() { return properties; }
}