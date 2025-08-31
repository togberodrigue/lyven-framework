package io.lyven.core.routing;

import io.lyven.core.annotations.*;
import io.lyven.core.di.Container;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Router for Lyven framework
 * Discovers and manages all routes automatically (like Angular Router)
 */
public class Router {

    private final Map<String, List<Route>> routesByMethod = new ConcurrentHashMap<>();
    private final List<Route> allRoutes = new ArrayList<>();
    private final RouteHandler routeHandler;
    private final Container container;

    public Router(Container container) {
        this.container = container;
        this.routeHandler = new RouteHandler();
        initializeRoutes();
    }

    /**
     * Initialize routes by scanning for @Component classes with HTTP annotations
     */
    private void initializeRoutes() {
        Set<Class<?>> components = container.getServiceRegistry().getAllComponents();

        for (Class<?> componentClass : components) {
            scanComponentForRoutes(componentClass);
        }

        System.out.println("🚀 Lyven Router initialized with " + allRoutes.size() + " routes");
        logAllRoutes();
    }

    /**
     * Scan a component class for HTTP method annotations
     */
    private void scanComponentForRoutes(Class<?> componentClass) {
        Object componentInstance = container.get(componentClass);
        Method[] methods = componentClass.getDeclaredMethods();

        for (Method method : methods) {
            Route route = createRouteFromMethod(method, componentInstance);
            if (route != null) {
                registerRoute(route);
            }
        }
    }

    /**
     * Create a Route from a method with HTTP annotations
     */
    private Route createRouteFromMethod(Method method, Object controller) {
        String path = null;
        String httpMethod = null;

        // Check for HTTP method annotations
        if (method.isAnnotationPresent(Get.class)) {
            Get getAnnotation = method.getAnnotation(Get.class);
            path = getAnnotation.value();
            httpMethod = "GET";
        } else if (method.isAnnotationPresent(Post.class)) {
            Post postAnnotation = method.getAnnotation(Post.class);
            path = postAnnotation.value();
            httpMethod = "POST";
        } else if (method.isAnnotationPresent(Put.class)) {
            Put putAnnotation = method.getAnnotation(Put.class);
            path = putAnnotation.value();
            httpMethod = "PUT";
        } else if (method.isAnnotationPresent(Delete.class)) {
            Delete deleteAnnotation = method.getAnnotation(Delete.class);
            path = deleteAnnotation.value();
            httpMethod = "DELETE";
        }

        if (path != null && httpMethod != null) {
            // If path is empty, use method name as path
            if (path.isEmpty()) {
                path = "/" + method.getName().toLowerCase();
            }

            // Ensure path starts with /
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            return Route.of(path, httpMethod, method, controller);
        }

        return null;
    }

    /**
     * Register a route in the routing table
     */
    private void registerRoute(Route route) {
        allRoutes.add(route);

        String httpMethod = route.httpMethod();
        routesByMethod.computeIfAbsent(httpMethod, k -> new ArrayList<>()).add(route);
    }

    /**
     * Find matching route for the given request
     */
    public Optional<Route> findRoute(String path, String httpMethod) {
        List<Route> methodRoutes = routesByMethod.get(httpMethod.toUpperCase());
        if (methodRoutes == null) {
            return Optional.empty();
        }

        return methodRoutes.stream()
                .filter(route -> route.matches(path, httpMethod))
                .findFirst();
    }

    /**
     * Execute a route with the given context
     */
    public Object executeRoute(String path, String httpMethod, RouteHandler.RouteContext context) {
        Optional<Route> routeOpt = findRoute(path, httpMethod);

        if (routeOpt.isEmpty()) {
            throw new RouteNotFoundException("No route found for " + httpMethod + " " + path);
        }

        Route route = routeOpt.get();
        return routeHandler.executeRoute(route, context);
    }

    /**
     * Get all registered routes
     */
    public List<Route> getAllRoutes() {
        return List.copyOf(allRoutes);
    }

    /**
     * Get routes by HTTP method
     */
    public List<Route> getRoutesByMethod(String httpMethod) {
        return List.copyOf(routesByMethod.getOrDefault(httpMethod.toUpperCase(), List.of()));
    }

    /**
     * Check if a route exists
     */
    public boolean hasRoute(String path, String httpMethod) {
        return findRoute(path, httpMethod).isPresent();
    }

    /**
     * Get route statistics
     */
    public Map<String, Object> getRouteStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", allRoutes.size());
        stats.put("routesByMethod", routesByMethod.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll));
        return stats;
    }

    /**
     * Log all registered routes (for debugging)
     */
    private void logAllRoutes() {
        System.out.println("📋 Registered routes:");
        allRoutes.forEach(route -> System.out.println("  " + route.getDescription()));
    }

    /**
     * Manually add a route (for testing or dynamic routes)
     */
    public void addRoute(Route route) {
        registerRoute(route);
    }

    /**
     * Remove all routes (for testing)
     */
    public void clearRoutes() {
        allRoutes.clear();
        routesByMethod.clear();
    }

    /**
     * Exception for when no route is found
     */
    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}