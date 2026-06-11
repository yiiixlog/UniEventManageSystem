package com.campusevent.storage;

import com.campusevent.model.Event;
import com.campusevent.model.Organizer;
import com.campusevent.model.Registration;
import com.campusevent.model.RegistrationStatus;
import com.campusevent.model.Student;
import com.campusevent.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FileStorageTest {

    @TempDir
    Path dataDirectory;

    @Test
    void loadUsersCreatesConcreteUserTypes() throws Exception {
        Files.write(dataDirectory.resolve("users.csv"), Arrays.asList(
                "role,userId,name,password,studentNo,department,employeeNo,organizationName",
                "STUDENT,S001,Alice,pass,S001,Design,,",
                "ORGANIZER,O001,Bob,secret,,,E001,Campus Office"
        ));

        List<User> users = new FileStorage(dataDirectory).loadUsers();

        assertEquals(2, users.size());
        assertInstanceOf(Student.class, users.get(0));
        assertInstanceOf(Organizer.class, users.get(1));
        assertEquals("Alice", users.get(0).getName());
        assertEquals("Campus Office", ((Organizer) users.get(1)).getOrganizationName());
    }

    @Test
    void saveAndLoadEventsRoundTrip() {
        FileStorage storage = new FileStorage(dataDirectory);
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 12, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 6, 12, 12, 0);

        storage.saveEvents(Arrays.asList(new Event(
                7,
                "AI Workshop",
                "Hands-on session",
                "Room 101",
                "Workshop",
                startTime,
                endTime,
                "Campus Office",
                30
        )));

        List<Event> events = storage.loadEvents();

        assertEquals(1, events.size());
        assertEquals(7, events.get(0).getEventId());
        assertEquals("AI Workshop", events.get(0).getTitle());
        assertEquals(startTime, events.get(0).getStartTime());
        assertEquals(30, events.get(0).getCapacity());
    }

    @Test
    void saveAndLoadRegistrationsRoundTrip() {
        FileStorage storage = new FileStorage(dataDirectory);
        LocalDateTime registeredAt = LocalDateTime.of(2026, 6, 10, 9, 30);

        storage.saveRegistrations(Arrays.asList(new Registration(
                3,
                "S001",
                7,
                registeredAt,
                RegistrationStatus.REGISTERED
        )));

        List<Registration> registrations = storage.loadRegistrations();

        assertEquals(1, registrations.size());
        assertEquals(3, registrations.get(0).getRegistrationId());
        assertEquals("S001", registrations.get(0).getStudentNo());
        assertEquals(registeredAt, registrations.get(0).getRegisteredAt());
        assertEquals(RegistrationStatus.REGISTERED, registrations.get(0).getStatus());
        assertEquals("REGISTERED", registrations.get(0).getStatusCode());
    }
}
