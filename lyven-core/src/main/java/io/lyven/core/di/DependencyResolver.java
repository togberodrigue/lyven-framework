package io.lyven.core.di;

import io.lyven.core.annotations.Component;
import io.lyven.core.annotations.Inject;
import io.lyven.core.annotations.Injectable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency Resolver for Lyven DI Container
 * Resolves constructor dependencies automatically (like Angular)
 */
public record DependencyResolver(Container container) {

    /**
     * Resolve all dependencies for a constructor
     */
    public Object[] resolveDependencies(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Object[] dependencies = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            dependencies[i] = resolveDependency(parameters[i]);
        }

        return dependencies;
    }

    /**
     * Resolve a single dependency parameter
     */
    private Object resolveDependency(Parameter parameter) {
        Class<?> dependencyType = parameter.getType();

        // Check if dependency is registered in container
        if (container.isRegistered(dependencyType)) {
            return container.get(dependencyType);
        }

        // Try to auto-register if it has @Component or @Injectable
        if (isAutoRegistrable(dependencyType)) {
            container.register(dependencyType);
            return container.get(dependencyType);
        }

        // Throw error if dependency cannot be resolved
        throw new DependencyResolutionException(
                "Cannot resolve dependency: " + dependencyType.getName() +
                        ". Make sure it's registered or annotated with @Component/@Injectable"
        );
    }

    /**
     * Check if a class can be auto-registered
     */
    private boolean isAutoRegistrable(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Injectable.class);
    }

    /**
     * Check for circular dependencies (basic implementation)
     */
    public boolean hasCircularDependency(Class<?> rootClass) {
        return hasCircularDependency(rootClass, new ArrayList<>());
    }

    private boolean hasCircularDependency(Class<?> currentClass, List<Class<?>> dependencyChain) {
        if (dependencyChain.contains(currentClass)) {
            return true; // Circular dependency detected
        }

        dependencyChain.add(currentClass);

        try {
            Constructor<?> constructor = findConstructor(currentClass);
            Parameter[] parameters = constructor.getParameters();

            for (Parameter parameter : parameters) {
                Class<?> dependencyType = parameter.getType();
                if (hasCircularDependency(dependencyType, new ArrayList<>(dependencyChain))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // If we can't analyze dependencies, assume no circular dependency
            return false;
        }

        return false;
    }

    /**
     * Find the best constructor for a class
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
     * Get dependency graph for debugging
     */
    public List<Class<?>> getDependencyChain(Class<?> rootClass) {
        List<Class<?>> chain = new ArrayList<>();
        buildDependencyChain(rootClass, chain);
        return chain;
    }

    private void buildDependencyChain(Class<?> currentClass, List<Class<?>> chain) {
        if (chain.contains(currentClass)) {
            return; // Avoid infinite loops
        }

        chain.add(currentClass);

        try {
            Constructor<?> constructor = findConstructor(currentClass);
            Parameter[] parameters = constructor.getParameters();

            for (Parameter parameter : parameters) {
                buildDependencyChain(parameter.getType(), chain);
            }
        } catch (Exception e) {
            // Ignore errors during chain building
        }
    }

    /**
     * Custom exception for dependency resolution failures
     */
    public static class DependencyResolutionException extends RuntimeException {
        public DependencyResolutionException(String message) {
            super(message);
        }

        public DependencyResolutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}