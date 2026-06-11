package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.storage.CsvUtils;
import com.campusevent.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    @TempDir
    Path dataDirectory;

    @TempDir
    Path exportDirectory;

    @Test
    void exportRegistrationsForEventWritesOnlyActiveRegistrationsWithStudentNames() throws Exception {
        Files.write(dataDirectory.resolve("users.csv"), Arrays.asList(
                "role,userId,name,password,studentNo,department,employeeNo,organizationName",
                "STUDENT,S001,Alice,pass,S001,Design,,",
                "STUDENT,S002,Ben,pass,S002,Design,,"
        ));
        Files.write(dataDirectory.resolve("registrations.csv"), Arrays.asList(
                "registrationId,studentNo,eventId,registeredAt,status",
                "1,S001,7,2026-06-10T09:30:00,REGISTERED",
                "2,S002,7,2026-06-10T09:45:00,CANCELLED",
                "3,S001,8,2026-06-10T10:00:00,REGISTERED"
        ));
        FileStorage storage = new FileStorage(dataDirectory);
        CsvExporter exporter = new CsvExporter(storage, new RegistrationService(storage), exportDirectory);
        Event event = new Event(
                7,
                "AI, Workshop",
                "Practice",
                "Room 202",
                "Workshop",
                LocalDateTime.of(2026, 6, 15, 9, 0),
                LocalDateTime.of(2026, 6, 15, 11, 0),
                "AI Club",
                30
        );

        Path exportedPath = exporter.exportRegistrationsForEvent(event);
        List<String> lines = Files.readAllLines(exportedPath);
        List<String> exportedRegistration = CsvUtils.parseLine(lines.get(1));

        assertEquals(exportDirectory.resolve("event_7_registrations.csv"), exportedPath);
        assertTrue(Files.exists(exportedPath));
        assertEquals(2, lines.size());
        assertEquals("eventId,eventTitle,studentNo,studentName,registeredAt,status", lines.get(0));
        assertEquals(Arrays.asList(
                "7",
                "AI, Workshop",
                "S001",
                "Alice",
                "2026-06-10T09:30:00",
                "REGISTERED"
        ), exportedRegistration);
    }
}
