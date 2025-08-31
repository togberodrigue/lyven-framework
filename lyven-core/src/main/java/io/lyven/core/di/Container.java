package io.lyven.core.di;

import io.lyven.core.annotations.Component;
import io.lyven.core.annotations.Injectable;
import io.lyven.core.annotations.Inject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Angular-like Dependency Injection Container for Lyven
 * Manages component lifecycle and dependencies
 */
public class Container {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final ServiceRegistry serviceRegistry;
    private final DependencyResolver dependencyResolver;

    public Container() {
        this.serviceRegistry = new ServiceRegistry();
        this.dependencyResolver = new DependencyResolver(this);
    }

    /**
     * Register a component class
     */
    public <T> void register(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Injectable.class)) {
            serviceRegistry.register(clazz);
        }
    }

    /**
     * Register with interface binding
     */
    public <T> void register(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        bindings.put(interfaceClass, implementationClass);
        register(implementationClass);
    }

    /**
     * Get instance of a class (Angular-like inject)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        // Check for interface binding
        if (bindings.containsKey(clazz)) {
            clazz = (Class<T>) bindings.get(clazz);
        }

        // Check if singleton
        if (isSingleton(clazz) && singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        }

        // Create new instance
        T instance = createInstance(clazz);

        // Store if singleton
        if (isSingleton(clazz)) {
            singletons.put(clazz, instance);
        }

        return instance;
    }

    /**
     * Create instance with dependency resolution
     */
    private <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = findConstructor(clazz);
            constructor.setAccessible(true);
            Object[] dependencies = dependencyResolver.resolveDependencies(constructor);
            return constructor.newInstance(dependencies);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Find the best constructor (prefer @Inject annotated or single constructor)
     */
    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // Single constructor - use it
        if (constructors.length == 1) {
            return (Constructor<T>) constructors[0];
        }

        // Look for @Inject annotated constructor
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return (Constructor<T>) constructor;
            }
        }

        // Fallback: use default constructor if available
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            // Last resort: use first constructor
            return (Constructor<T>) constructors[0];
        }
    }

    /**
     * Check if class should be singleton
     */
    private boolean isSingleton(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return clazz.getAnnotation(Component.class).singleton();
        }
        if (clazz.isAnnotationPresent(Injectable.class)) {
            return clazz.getAnnotation(Injectable.class).singleton();
        }
        return true; // Default to singleton
    }

    /**
     * Check if class is registered
     */
    public boolean isRegistered(Class<?> clazz) {
        return serviceRegistry.isRegistered(clazz) || bindings.containsKey(clazz);
    }

    /**
     * Get service registry
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}