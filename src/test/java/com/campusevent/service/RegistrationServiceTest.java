package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.RegistrationStatus;
import com.campusevent.model.Student;
import com.campusevent.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationServiceTest {

    @TempDir
    Path dataDirectory;

    private FileStorage storage;
    private RegistrationService service;
    private Event event;
    private Student student;

    @BeforeEach
    void setUp() throws Exception {
        Files.write(dataDirectory.resolve("registrations.csv"), Arrays.asList(
                "registrationId,studentNo,eventId,registeredAt,status"
        ));
        storage = new FileStorage(dataDirectory);
        service = new RegistrationService(storage);
        event = new Event(
                10,
                "AI Workshop",
                "Practice",
                "Room 202",
                "Workshop",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "AI Club",
                1
        );
        student = new Student("S001", "Alice", "pass", "S001", "Design");
    }

    @Test
    void registerCreatesActiveRegistration() {
        Registration registration = service.register(student, event);

        assertEquals(1, registration.getRegistrationId());
        assertEquals("S001", registration.getStudentNo());
        assertEquals(10, registration.getEventId());
        assertTrue(registration.isActive());
        assertEquals(1, service.countActiveRegistrations(10));
    }

    @Test
    void registerRejectsDuplicateActiveRegistration() {
        service.register(student, event);

        assertThrows(IllegalArgumentException.class, () -> service.register(student, event));
    }

    @Test
    void registerRejectsWhenCapacityIsFull() {
        Student anotherStudent = new Student("S002", "Ben", "pass", "S002", "Design");
        service.register(student, event);

        assertThrows(IllegalArgumentException.class, () -> service.register(anotherStudent, event));
    }

    @Test
    void cancelRegistrationMarksRecordInactive() {
        Registration registration = service.register(student, event);

        service.cancelRegistration(registration.getRegistrationId());

        List<Registration> registrations = storage.loadRegistrations();
        assertEquals(1, registrations.size());
        assertFalse(registrations.get(0).isActive());
        assertEquals(RegistrationStatus.CANCELLED, registrations.get(0).getStatus());
    }

    @Test
    void markRegistrationsDeletedByEventOnlyUpdatesActiveRecords() {
        service.register(student, event);
        service.cancelRegistration(1);
        storage.saveRegistrations(Arrays.asList(
                new Registration(1, "S001", 10, LocalDateTime.now(), RegistrationStatus.CANCELLED),
                new Registration(2, "S002", 10, LocalDateTime.now(), RegistrationStatus.REGISTERED)
        ));

        service.markRegistrationsDeletedByEvent(10);

        List<Registration> registrations = storage.loadRegistrations();
        assertEquals(RegistrationStatus.CANCELLED, registrations.get(0).getStatus());
        assertEquals(RegistrationStatus.EVENT_DELETED, registrations.get(1).getStatus());
    }
}
