package io.lyven.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reflection utilities for Lyven framework
 * Provides safe and convenient reflection operations
 */
public class ReflectionUtils {

    /**
     * Create instance of a class using default constructor
     */
    public static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Create instance using specific constructor
     */
    public static <T> T createInstance(Constructor<T> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance using constructor", e);
        }
    }

    /**
     * Get all fields of a class (including inherited fields)
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        while (clazz != null && !clazz.equals(Object.class)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Get all methods of a class (including inherited methods)
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();

        while (clazz != null && !clazz.equals(Object.class)) {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }

        return methods;
    }

    /**
     * Get field value safely
     */
    public static Object getFieldValue(Object instance, String fieldName) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(instance);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
        return null;
    }

    /**
     * Set field value safely
     */
    public static void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value: " + fieldName, e);
        }
    }

    /**
     * Find field by name in class hierarchy
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Find method by name and parameter types
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Invoke method safely
     */
    public static Object invokeMethod(Object instance, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(args)
                    .map(arg -> arg != null ? arg.getClass() : Object.class)
                    .toArray(Class[]::new);

            Method method = findMethod(instance.getClass(), methodName, paramTypes);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(instance, args);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
        return null;
    }

    /**
     * Get all fields annotated with specific annotation
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    /**
     * Get all methods annotated with specific annotation
     */
    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getAllMethods(clazz).stream()
                .filter(method -> method.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    /**
     * Check if class has annotation
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return clazz.isAnnotationPresent(annotationType);
    }

    /**
     * Get annotation from class or null if not present
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        return clazz.getAnnotation(annotationType);
    }

    /**
     * Get all constructors of a class
     */
    public static List<Constructor<?>> getAllConstructors(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredConstructors());
    }

    /**
     * Find constructor by parameter types
     */
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Get parameter names for method (requires compilation with -parameters)
     */
    public static String[] getParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        return Arrays.stream(parameters)
                .map(Parameter::getName)
                .toArray(String[]::new);
    }

    /**
     * Get parameter names for constructor
     */
    public static String[] getParameterNames(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        return Arrays.stream(parameters)
                .map(Parameter::getName)
                .toArray(String[]::new);
    }

    /**
     * Check if method is getter
     */
    public static boolean isGetter(Method method) {
        String name = method.getName();
        return method.getParameterCount() == 0 &&
                !method.getReturnType().equals(void.class) &&
                (name.startsWith("get") || (name.startsWith("is") &&
                        (method.getReturnType().equals(boolean.class) ||
                                method.getReturnType().equals(Boolean.class))));
    }

    /**
     * Check if method is setter
     */
    public static boolean isSetter(Method method) {
        String name = method.getName();
        return method.getParameterCount() == 1 &&
                method.getReturnType().equals(void.class) &&
                name.startsWith("set");
    }

    /**
     * Get property name from getter/setter method
     */
    public static String getPropertyName(Method method) {
        String name = method.getName();

        if (name.startsWith("get") || name.startsWith("set")) {
            return StringUtils.uncapitalize(name.substring(3));
        } else if (name.startsWith("is")) {
            return StringUtils.uncapitalize(name.substring(2));
        }

        return name;
    }

    /**
     * Check if class is abstract
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Check if class is interface
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    /**
     * Check if class is concrete (not abstract or interface)
     */
    public static boolean isConcrete(Class<?> clazz) {
        return !isAbstract(clazz) && !isInterface(clazz);
    }

    /**
     * Get all interfaces implemented by class
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new HashSet<>();

        while (clazz != null) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }

        return new ArrayList<>(interfaces);
    }

    /**
     * Check if object is instance of any of the given classes
     */
    public static boolean isInstanceOfAny(Object object, Class<?>... classes) {
        if (object == null || classes == null) {
            return false;
        }

        Class<?> objectClass = object.getClass();
        return Arrays.stream(classes)
                .anyMatch(clazz -> clazz.isAssignableFrom(objectClass));
    }

    /**
     * Create deep copy of object using reflection (simple implementation)
     */
    public static <T> T deepCopy(T original) {
        if (original == null) {
            return null;
        }

        // This is a simplified implementation
        // For production, consider using libraries like Apache Commons Lang
        throw new UnsupportedOperationException("Deep copy not implemented yet");
    }
}