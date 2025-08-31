package io.lyven.core.example;
import io.lyven.core.annotations.Injectable;

@Injectable
class UserService {

    public String findAllUsers() {
        return "List of users from service";
    }

    public String createUser(String userData) {
        return "User created: " + userData;
    }
}