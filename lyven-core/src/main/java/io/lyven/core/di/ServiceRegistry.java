package io.lyven.core.di;

import io.lyven.core.annotations.Component;
import io.lyven.core.annotations.Injectable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Service Registry for managing registered components and services
 * Similar to Angular's providedIn system
 */
public class ServiceRegistry {

    private final Set<Class<?>> registeredServices = new CopyOnWriteArraySet<>();
    private final Set<Class<?>> components = new CopyOnWriteArraySet<>();
    private final Set<Class<?>> injectables = new CopyOnWriteArraySet<>();

    /**
     * Register a service/component class
     */
    public void register(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            components.add(clazz);
            registeredServices.add(clazz);
        } else if (clazz.isAnnotationPresent(Injectable.class)) {
            injectables.add(clazz);
            registeredServices.add(clazz);
        }
    }

    /**
     * Check if a class is registered
     */
    public boolean isRegistered(Class<?> clazz) {
        return registeredServices.contains(clazz);
    }

    /**
     * Get all registered services
     */
    public Set<Class<?>> getAllRegisteredServices() {
        return Set.copyOf(registeredServices);
    }

    /**
     * Get all components (@Component annotated)
     */
    public Set<Class<?>> getAllComponents() {
        return Set.copyOf(components);
    }

    /**
     * Get all injectables (@Injectable annotated)
     */
    public Set<Class<?>> getAllInjectables() {
        return Set.copyOf(injectables);
    }

    /**
     * Get component selector (if available)
     */
    public String getComponentSelector(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            return component.selector().isEmpty() ? clazz.getSimpleName().toLowerCase() : component.selector();
        }
        return null;
    }

    /**
     * Get component providers (dependencies)
     */
    public Class<?>[] getComponentProviders(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            return component.providers();
        }
        return new Class<?>[0];
    }

    /**
     * Clear all registrations (for testing)
     */
    public void clear() {
        registeredServices.clear();
        components.clear();
        injectables.clear();
    }

    /**
     * Get registration count
     */
    public int getRegistrationCount() {
        return registeredServices.size();
    }

    /**
     * Check if class is a component
     */
    public boolean isComponent(Class<?> clazz) {
        return components.contains(clazz);
    }

    /**
     * Check if class is an injectable service
     */
    public boolean isInjectable(Class<?> clazz) {
        return injectables.contains(clazz);
    }
}