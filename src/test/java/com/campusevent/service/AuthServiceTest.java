package com.campusevent.service;

import com.campusevent.model.User;
import com.campusevent.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @TempDir
    Path dataDirectory;

    @Test
    void loginReturnsUserForMatchingAccountAndPassword() throws Exception {
        Files.write(dataDirectory.resolve("users.csv"), Arrays.asList(
                "role,userId,name,password,studentNo,department,employeeNo,organizationName",
                "STUDENT,S001,Alice,pass,S001,Design,,"
        ));
        AuthService service = new AuthService(new FileStorage(dataDirectory));

        Optional<User> user = service.login("S001", "pass");

        assertTrue(user.isPresent());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        Files.write(dataDirectory.resolve("users.csv"), Arrays.asList(
                "role,userId,name,password,studentNo,department,employeeNo,organizationName",
                "STUDENT,S001,Alice,pass,S001,Design,,"
        ));
        AuthService service = new AuthService(new FileStorage(dataDirectory));

        Optional<User> user = service.login("S001", "wrong");

        assertFalse(user.isPresent());
    }
}
