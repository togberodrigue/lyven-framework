package io.lyven.core.example;

import io.lyven.core.annotations.*;
import io.lyven.core.reactive.Observable;

@Component(
        selector = "user-controller"
)
record UserController(UserService userService) {

    @Inject
    UserController {
    }

    @Get("/users")
    public Observable<String> getUsers() {
        return Observable.of(userService.findAllUsers());
    }

    @Post("/users")
    public Observable<String> createUser(@Body String userData) {
        return Observable.of(userService.createUser(userData));
    }

    @Get("/users/{id}")
    public Observable<String> getUserById(Long id) {
        return Observable.of("User " + id + " details");
    }
}