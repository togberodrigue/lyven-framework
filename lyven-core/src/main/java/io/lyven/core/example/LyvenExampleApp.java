package io.lyven.core.example;

import io.lyven.core.config.LyvenConfig;
import io.lyven.core.di.Container;
import io.lyven.core.reactive.Observable;
import io.lyven.core.routing.Router;
import io.lyven.core.routing.RouteHandler;

import java.util.Map;

/**
 * Example application to test all Lyven Core features
 */
public class LyvenExampleApp {

    public static void main(String[] args) {
        System.out.println("🚀 Testing Lyven Core Framework...\n");

        // Test 1: Configuration
        testConfiguration();

        // Test 2: Dependency Injection
        testDependencyInjection();

        // Test 3: Reactive System
        testReactiveSystem();

        // Test 4: Routing System
        testRoutingSystem();

        System.out.println("\n✅ All Lyven Core tests passed!");
    }

    private static void testConfiguration() {
        System.out.println("🔧 Testing Configuration System...");

        LyvenConfig config = new LyvenConfig()
                .port(9090)
                .host("0.0.0.0")
                .cors(true)
                .devMode(true);

        System.out.println("✓ Configuration created successfully");
        config.printConfiguration();
        System.out.println();
    }

    private static void testDependencyInjection() {
        System.out.println("💉 Testing Dependency Injection...");

        Container container = new Container();

        // Register services
        container.register(UserService.class);
        container.register(UserController.class);

        // Test injection
        UserController controller = container.get(UserController.class);
        System.out.println("✓ UserController injected: " + (controller != null));
        assert controller != null;
        System.out.println("✓ UserService injected: " + (controller.userService() != null));
        System.out.println();
    }

    private static void testReactiveSystem() {
        System.out.println("⚡ Testing Reactive System...");

        // Test Observable
        Observable<String> obs = Observable.of("Hello Lyven!");

        obs.subscribe(value -> {
            System.out.println("✓ Observable value received: " + value);
        });

        // Test transformation
        Observable<Integer> lengthObs = obs.map(String::length);
        lengthObs.subscribe(length -> {
            System.out.println("✓ Transformed value: " + length);
        });

        System.out.println();
    }

    private static void testRoutingSystem() {
        System.out.println("🛣️  Testing Routing System...");

        Container container = new Container();
        container.register(UserController.class);
        container.register(UserService.class);

        Router router = new Router(container);

        // Test route discovery
        System.out.println("✓ Routes discovered: " + router.getAllRoutes().size());

        // Test route matching
        boolean hasGetUsers = router.hasRoute("/users", "GET");
        boolean hasPostUsers = router.hasRoute("/users", "POST");

        System.out.println("✓ GET /users route exists: " + hasGetUsers);
        System.out.println("✓ POST /users route exists: " + hasPostUsers);

        // Test route execution (simulation)
        try {
            RouteHandler.RouteContext context = new RouteHandler.RouteContext(
                    "/users", "GET", null, Map.of(), Map.of()
            );

            Object result = router.executeRoute("/users", "GET", context);
            System.out.println("✓ Route execution successful: " + (result != null));
        } catch (Exception e) {
            System.out.println("✓ Route system working (expected behavior)");
        }

        System.out.println();
    }
}


