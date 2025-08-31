package io.lyven.core.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Properties management for Lyven configuration
 * Handles loading from files, environment variables, and system properties
 */
public class Properties {

    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private final Map<String, String> defaultValues = new HashMap<>();

    public Properties() {
        loadDefaultProperties();
    }

    /**
     * Load default Lyven properties
     */
    private void loadDefaultProperties() {
        // Server configuration
        defaultValues.put("server.port", "8080");
        defaultValues.put("server.host", "localhost");
        defaultValues.put("server.context-path", "");

        // CORS configuration
        defaultValues.put("server.cors.enabled", "true");
        defaultValues.put("server.cors.allowed-origins", "*");
        defaultValues.put("server.cors.allowed-methods", "GET,POST,PUT,DELETE,OPTIONS");
        defaultValues.put("server.cors.allowed-headers", "*");

        // Lyven framework configuration
        defaultValues.put("lyven.dev-mode", "false");
        defaultValues.put("lyven.auto-scan.enabled", "true");
        defaultValues.put("lyven.auto-scan.packages", "io.lyven");

        // Logging configuration
        defaultValues.put("logging.level", "INFO");
        defaultValues.put("logging.pattern", "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");

        // JSON configuration
        defaultValues.put("json.fail-on-unknown-properties", "false");
        defaultValues.put("json.fail-on-empty-beans", "false");

        // Database configuration (for future use)
        defaultValues.put("database.auto-create-schema", "true");
        defaultValues.put("database.show-sql", "false");
    }

    /**
     * Load properties from a file
     */
    public void loadFromFile(String filename) {
        try {
            // Try to load from classpath first
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);

            if (inputStream == null) {
                // Try to load from file system
                File file = new File(filename);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                }
            }

            if (inputStream != null) {
                java.util.Properties props = new java.util.Properties();
                props.load(inputStream);

                for (String key : props.stringPropertyNames()) {
                    properties.put(key, props.getProperty(key));
                }

                System.out.println("📄 Loaded properties from: " + filename);
                inputStream.close();
            } else {
                System.out.println("ℹ️  Properties file not found: " + filename + " (using defaults)");
            }
        } catch (IOException e) {
            System.err.println("⚠️  Failed to load properties from: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Load properties from environment variables
     */
    public void loadFromEnvironment(String prefix) {
        Map<String, String> env = System.getenv();

        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                // Convert LYVEN_SERVER_PORT to server.port
                String propertyKey = key.substring(prefix.length())
                        .toLowerCase()
                        .replace('_', '.');
                properties.put(propertyKey, entry.getValue());
            }
        }

        System.out.println("🌍 Loaded environment variables with prefix: " + prefix);
    }

    /**
     * Load from system properties
     */
    public void loadFromSystemProperties() {
        java.util.Properties sysProps = System.getProperties();

        for (String key : sysProps.stringPropertyNames()) {
            if (key.startsWith("lyven.")) {
                properties.put(key, sysProps.getProperty(key));
            }
        }

        System.out.println("⚙️  Loaded system properties");
    }

    /**
     * Get string property
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Get string property with default value
     */
    public String getString(String key, String defaultValue) {
        String value = properties.get(key);
        if (value != null) {
            return value;
        }

        value = defaultValues.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get integer property
     */
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("⚠️  Invalid integer value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    /**
     * Get boolean property
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Get long property
     */
    public long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.err.println("⚠️  Invalid long value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    /**
     * Get double property
     */
    public double getDouble(String key, double defaultValue) {
        String value = getString(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.err.println("⚠️  Invalid double value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    /**
     * Set property programmatically
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Check if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key) || defaultValues.containsKey(key);
    }

    /**
     * Get all properties (including defaults)
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> allProps = new HashMap<>(defaultValues);
        allProps.putAll(properties);
        return allProps;
    }

    /**
     * Get only custom properties (excluding defaults)
     */
    public Map<String, String> getCustomProperties() {
        return new HashMap<>(properties);
    }

    /**
     * Print all properties for debugging
     */
    public void printProperties() {
        System.out.println("📋 Current Lyven Properties:");
        Map<String, String> allProps = getAllProperties();
        allProps.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println("  " + entry.getKey() + " = " + entry.getValue()));
    }

    /**
     * Save properties to file
     */
    public void saveToFile(String filename) throws IOException {
        java.util.Properties props = new java.util.Properties();
        props.putAll(properties);

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            props.store(fos, "Lyven Configuration Properties");
        }

        System.out.println("💾 Properties saved to: " + filename);
    }

    /**
     * Clear all custom properties (keep defaults)
     */
    public void clear() {
        properties.clear();
    }

    /**
     * Get property count
     */
    public int size() {
        return getAllProperties().size();
    }
}