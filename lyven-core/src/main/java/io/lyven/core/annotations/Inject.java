package io.lyven.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Angular-like @Inject annotation for constructor injection
 * Marks the preferred constructor for dependency injection
 */
@Target({ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * Optional qualifier for the injection
     */
    String value() default "";

    /**
     * Whether this dependency is optional
     */
    boolean optional() default false;
}