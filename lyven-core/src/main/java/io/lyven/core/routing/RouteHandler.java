package io.lyven.core.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lyven.core.annotations.*;
import io.lyven.core.reactive.Observable;
import io.lyven.core.reactive.ReactiveUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Handles route execution and parameter binding
 * Similar to Angular's HTTP interceptors but for method execution
 */
public class RouteHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Execute a route with the given request context
     */
    public Object executeRoute(Route route, RouteContext context) {
        try {
            Object[] methodArgs = resolveMethodArguments(route, context);
            Object result = route.handler().invoke(route.controller(), methodArgs);

            // Handle different return types
            return processResult(result);

        } catch (Exception e) {
            throw new RouteExecutionException("Failed to execute route: " + route.getDescription(), e);
        }
    }

    /**
     * Resolve method arguments from request context
     */
    private Object[] resolveMethodArguments(Route route, RouteContext context) {
        Method method = route.handler();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Map<String, String> pathVariables = route.extractPathVariables(context.path());

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveParameter(parameters[i], context, pathVariables);
        }

        return args;
    }

    /**
     * Resolve a single method parameter
     */
    private Object resolveParameter(Parameter parameter, RouteContext context, Map<String, String> pathVariables) {
        // Handle @Body annotation
        if (parameter.isAnnotationPresent(Body.class)) {
            return parseRequestBody(parameter.getType(), context.body());
        }

        // Handle path variables (like @PathVariable in Spring)
        String paramName = parameter.getName();
        if (pathVariables.containsKey(paramName)) {
            return convertPathVariable(pathVariables.get(paramName), parameter.getType());
        }

        // Handle query parameters
        if (context.queryParams().containsKey(paramName)) {
            return convertQueryParam(context.queryParams().get(paramName), parameter.getType());
        }

        // Handle request/response objects
        if (parameter.getType().equals(RouteContext.class)) {
            return context;
        }

        // Default: try to inject from DI container (future enhancement)
        return null;
    }

    /**
     * Parse request body to the expected type
     */
    private Object parseRequestBody(Class<?> targetType, String requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }

        // Handle String directly
        if (targetType.equals(String.class)) {
            return requestBody;
        }

        // Parse JSON to target type
        try {
            return objectMapper.readValue(requestBody, targetType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON body to " + targetType.getSimpleName(), e);
        }
    }
    /**
     * Convert path variable string to target type
     */
    private Object convertPathVariable(String value, Class<?> targetType) {
        return convertStringToType(value, targetType);
    }

    /**
     * Convert query parameter to target type
     */
    private Object convertQueryParam(String value, Class<?> targetType) {
        return convertStringToType(value, targetType);
    }

    /**
     * Generic string to type conversion
     */
    private Object convertStringToType(String value, Class<?> targetType) {
        if (targetType.equals(String.class)) {
            return value;
        } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            return Long.parseLong(value);
        } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return Double.parseDouble(value);
        }

        throw new IllegalArgumentException("Type conversion for " + targetType + " not supported");
    }

    /**
     * Process method result (handle async, Observable, etc.)
     */
    private Object processResult(Object result) {
        if (result == null) {
            return null;
        }

        // Handle Observable results
        if (result instanceof Observable) {
            return result;
        }

        // Handle CompletableFuture
        if (result instanceof CompletableFuture) {
            return ReactiveUtils.fromFuture((CompletableFuture<?>) result);
        }

        // Regular objects
        return result;
    }

    /**
         * Route context containing request information
         */
        public record RouteContext(String path, String method, String body, Map<String, String> queryParams,
                                   Map<String, String> headers) {
    }

    /**
     * Exception for route execution failures
     */
    public static class RouteExecutionException extends RuntimeException {
        public RouteExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}