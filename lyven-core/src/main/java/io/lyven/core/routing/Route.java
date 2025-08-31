package io.lyven.core.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a single route in Lyven routing system
 * Similar to Angular Route but for backend endpoints
 */
public record Route(
        String path,
        String httpMethod,
        Method handler,
        Object controller,
        Pattern pathPattern,
        String[] pathParams
) {

    /**
     * Create a new Route
     */
    public static Route of(String path, String httpMethod, Method handler, Object controller) {
        // Convert path like "/users/{id}" to regex pattern
        String[] pathParams = extractPathParams(path);
        Pattern pathPattern = createPathPattern(path);

        return new Route(path, httpMethod, handler, controller, pathPattern, pathParams);
    }

    /**
     * Check if this route matches the given path and HTTP method
     */
    public boolean matches(String requestPath, String requestMethod) {
        return httpMethod.equalsIgnoreCase(requestMethod) &&
                pathPattern.matcher(requestPath).matches();
    }

    /**
     * Extract path parameters from request path
     * Example: "/users/123" -> {"id": "123"} for pattern "/users/{id}"
     */
    public Map<String, String> extractPathVariables(String requestPath) {
        var matcher = pathPattern.matcher(requestPath);
        if (!matcher.matches()) {
            return Map.of();
        }

        Map<String, String> variables = new java.util.HashMap<>();
        for (int i = 0; i < pathParams.length; i++) {
            variables.put(pathParams[i], matcher.group(i + 1));
        }
        return variables;
    }

    /**
     * Extract parameter names from path like "/users/{id}/posts/{postId}"
     */
    private static String[] extractPathParams(String path) {
        return java.util.regex.Pattern.compile("\\{([^}]+)}")
                .matcher(path)
                .results()
                .map(match -> match.group(1))
                .toArray(String[]::new);
    }

    /**
     * Convert path template to regex pattern
     * "/users/{id}" -> "/users/([^/]+)"
     */
    private static Pattern createPathPattern(String path) {
        String regex = path.replaceAll("\\{[^}]+}", "([^/]+)");
        return Pattern.compile("^" + regex + "$");
    }

    /**
     * Get route description for logging
     */
    public String getDescription() {
        return String.format("%s %s -> %s.%s",
                httpMethod,
                path,
                controller.getClass().getSimpleName(),
                handler.getName()
        );
    }

    /**
     * Check if route has path parameters
     */
    public boolean hasPathParameters() {
        return pathParams.length > 0;
    }

    /**
     * Get parameter count
     */
    public int getParameterCount() {
        return pathParams.length;
    }
}