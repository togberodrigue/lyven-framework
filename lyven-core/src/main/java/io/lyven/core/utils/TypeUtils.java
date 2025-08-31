package io.lyven.core.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Type utilities for Lyven framework
 * Helps with generic type resolution and type checking
 */
public class TypeUtils {

    /**
     * Check if a class is a primitive type or wrapper
     */
    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) ||
                type.equals(Byte.class) ||
                type.equals(Character.class) ||
                type.equals(Short.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Float.class) ||
                type.equals(Double.class) ||
                type.equals(String.class);
    }

    /**
     * Check if a class is a collection type
     */
    public static boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    /**
     * Check if a class is a map type
     */
    public static boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    /**
     * Check if a class is an array type
     */
    public static boolean isArray(Class<?> type) {
        return type.isArray();
    }

    /**
     * Check if a class is an Optional type
     */
    public static boolean isOptional(Class<?> type) {
        return Optional.class.isAssignableFrom(type);
    }

    /**
     * Get the generic type of a parameterized type
     * Example: List<String> -> String.class
     */
    public static Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            if (actualTypes.length > 0 && actualTypes[0] instanceof Class) {
                return (Class<?>) actualTypes[0];
            }
        }
        return Object.class;
    }

    /**
     * Get all generic types of a parameterized type
     * Example: Map<String, Integer> -> [String.class, Integer.class]
     */
    public static Class<?>[] getAllGenericTypes(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            Class<?>[] classes = new Class[actualTypes.length];

            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] instanceof Class) {
                    classes[i] = (Class<?>) actualTypes[i];
                } else {
                    classes[i] = Object.class;
                }
            }
            return classes;
        }
        return new Class[0];
    }

    /**
     * Check if one type is assignable from another
     */
    public static boolean isAssignable(Class<?> target, Class<?> source) {
        if (target == null || source == null) {
            return false;
        }
        return target.isAssignableFrom(source);
    }

    /**
     * Get wrapper class for primitive type
     */
    public static Class<?> getWrapperType(Class<?> primitiveType) {
        if (!primitiveType.isPrimitive()) {
            return primitiveType;
        }

        return switch (primitiveType.getName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            default -> primitiveType;
        };
    }

    /**
     * Get primitive class for wrapper type
     */
    public static Class<?> getPrimitiveType(Class<?> wrapperType) {
        return switch (wrapperType.getName()) {
            case "java.lang.Boolean" -> boolean.class;
            case "java.lang.Byte" -> byte.class;
            case "java.lang.Character" -> char.class;
            case "java.lang.Short" -> short.class;
            case "java.lang.Integer" -> int.class;
            case "java.lang.Long" -> long.class;
            case "java.lang.Float" -> float.class;
            case "java.lang.Double" -> double.class;
            default -> wrapperType;
        };
    }

    /**
     * Check if a type is a numeric type
     */
    public static boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type) ||
                type.equals(byte.class) ||
                type.equals(short.class) ||
                type.equals(int.class) ||
                type.equals(long.class) ||
                type.equals(float.class) ||
                type.equals(double.class);
    }

    /**
     * Convert string value to target type
     */
    public static Object convertStringToType(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.equals(String.class)) {
            return value;
        }

        try {
            if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (targetType.equals(byte.class) || targetType.equals(Byte.class)) {
                return Byte.parseByte(value);
            } else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
                return Short.parseShort(value);
            } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
                return Long.parseLong(value);
            } else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
                return Float.parseFloat(value);
            } else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
                return Double.parseDouble(value);
            } else if (targetType.equals(char.class) || targetType.equals(Character.class)) {
                return !value.isEmpty() ? value.charAt(0) : '\0';
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert '" + value + "' to " + targetType.getSimpleName(), e);
        }

        throw new IllegalArgumentException("Unsupported type conversion: " + targetType.getSimpleName());
    }

    /**
     * Get simple type name for logging/debugging
     */
    public static String getSimpleTypeName(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getSimpleName();
        } else if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            Type[] args = parameterizedType.getActualTypeArguments();

            StringBuilder sb = new StringBuilder(rawType.getSimpleName());
            sb.append("<");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(getSimpleTypeName(args[i]));
            }
            sb.append(">");
            return sb.toString();
        }
        return type.toString();
    }

    /**
     * Check if a class has a default (no-args) constructor
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}