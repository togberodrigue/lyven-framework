package io.lyven.core.config;

import io.lyven.core.annotations.Component;
import io.lyven.core.annotations.Injectable;
import io.lyven.core.di.Container;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Auto-configuration system for Lyven
 * Automatically discovers and registers components (like Spring Boot auto-config)
 */
public class AutoConfiguration {

    private final Container container;
    private final List<String> scanPackages;
    private boolean autoScanEnabled = true;

    public AutoConfiguration(Container container) {
        this.container = container;
        this.scanPackages = new ArrayList<>();

        // Add default scan packages
        scanPackages.add("io.lyven");
    }

    /**
     * Run auto-configuration
     */
    public void configure() {
        if (!autoScanEnabled) {
            return;
        }

        System.out.println("🔍 Starting Lyven auto-configuration...");

        // Scan for components
        Set<Class<?>> discoveredComponents = scanForComponents();

        // Register discovered components
        registerComponents(discoveredComponents);

        System.out.println("✅ Auto-configuration completed. Registered " +
                discoveredComponents.size() + " components");
    }

    /**
     * Scan packages for @Component and @Injectable classes
     */
    private Set<Class<?>> scanForComponents() {
        Set<Class<?>> components = new java.util.HashSet<>();

        for (String packageName : scanPackages) {
            components.addAll(scanPackage(packageName));
        }

        return components;
    }

    /**
     * Scan a specific package for annotated classes
     */
    private Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> classes = new java.util.HashSet<>();

        try {
            // Get package path
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    classes.addAll(findClasses(directory, packageName));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️  Failed to scan package: " + packageName + " - " + e.getMessage());
        }

        return classes;
    }

    /**
     * Recursively find classes in a directory
     */
    private List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (isComponentClass(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore classes that can't be loaded
                }
            }
        }

        return classes;
    }

    /**
     * Check if a class should be registered as a component
     */
    private boolean isComponentClass(Class<?> clazz) {
        // Skip abstract classes, interfaces, and inner classes
        if (clazz.isInterface() ||
                Modifier.isAbstract(clazz.getModifiers()) ||
                clazz.isMemberClass()) {
            return false;
        }

        // Check for Lyven annotations
        return clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Injectable.class);
    }

    /**
     * Register discovered components in the container
     */
    private void registerComponents(Set<Class<?>> components) {
        for (Class<?> componentClass : components) {
            try {
                container.register(componentClass);
                System.out.println("📦 Registered component: " + componentClass.getSimpleName());
            } catch (Exception e) {
                System.err.println("❌ Failed to register component: " +
                        componentClass.getSimpleName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Add package to scan
     */
    public AutoConfiguration addScanPackage(String packageName) {
        if (!scanPackages.contains(packageName)) {
            scanPackages.add(packageName);
        }
        return this;
    }

    /**
     * Remove package from scan
     */
    public AutoConfiguration removeScanPackage(String packageName) {
        scanPackages.remove(packageName);
        return this;
    }

    /**
     * Enable/disable auto-scanning
     */
    public AutoConfiguration enableAutoScan(boolean enabled) {
        this.autoScanEnabled = enabled;
        return this;
    }

    /**
     * Get all scan packages
     */
    public List<String> getScanPackages() {
        return List.copyOf(scanPackages);
    }

    /**
     * Manually register a component (bypass scanning)
     */
    public AutoConfiguration registerComponent(Class<?> componentClass) {
        container.register(componentClass);
        return this;
    }

    /**
     * Register component with interface binding
     */
    public <T> AutoConfiguration registerComponent(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        container.register(interfaceClass, implementationClass);
        return this;
    }

    /**
     * Get configuration summary
     */
    public void printSummary() {
        System.out.println("🔧 Auto-Configuration Summary:");
        System.out.println("  Auto-scan: " + (autoScanEnabled ? "enabled" : "disabled"));
        System.out.println("  Scan packages: " + scanPackages);
        System.out.println("  Registered components: " + container.getServiceRegistry().getRegistrationCount());
    }

    /**
     * Check if auto-scan is enabled
     */
    public boolean isAutoScanEnabled() {
        return autoScanEnabled;
    }
}