package service;

import model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private final Map<UUID, User> users;

    public UserService() {
        this.users = new HashMap<>();
    }

    public User getUser(UUID userId) {
        return users.computeIfAbsent(userId, k -> new User());
    }

    public UUID createUser() {
        User user = new User();
        users.put(user.getId(), user);
        return user.getId();
    }
}