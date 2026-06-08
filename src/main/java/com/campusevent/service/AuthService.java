package com.campusevent.service;

import com.campusevent.model.User;
import com.campusevent.storage.FileStorage;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final FileStorage fileStorage;

    public AuthService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public Optional<User> login(String account, String password) {
        List<User> users = fileStorage.loadUsers();
        return users.stream()
                .filter(user -> user.getUserId().equals(account))
                .filter(user -> user.checkPassword(password))
                .findFirst();
    }
}
