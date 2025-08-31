package io.lyven.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Angular-like @Component annotation for Lyven
 *
 * Usage:
 * @Component({
 *   selector: "user-service",
 *   providers: [DatabaseService.class]
 * })
 * export class UserService { ... }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * Component selector (similar to Angular)
     */
    String selector() default "";

    /**
     * Dependencies/providers for this component
     */
    Class<?>[] providers() default {};

    /**
     * Component template path (for future use)
     */
    String template() default "";

    /**
     * Whether this component is a singleton
     */
    boolean singleton() default true;
}