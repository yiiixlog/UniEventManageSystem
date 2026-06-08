package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import com.campusevent.model.User;
import com.campusevent.storage.CsvUtils;
import com.campusevent.storage.FileStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CsvExporter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FileStorage fileStorage;
    private final RegistrationService registrationService;
    private final Path exportDirectory;

    public CsvExporter(FileStorage fileStorage, RegistrationService registrationService) {
        this(fileStorage, registrationService, Paths.get("exports"));
    }

    public CsvExporter(FileStorage fileStorage, RegistrationService registrationService, Path exportDirectory) {
        this.fileStorage = fileStorage;
        this.registrationService = registrationService;
        this.exportDirectory = exportDirectory;
    }

    public Path exportRegistrationsForEvent(Event event) {
        List<Registration> registrations = registrationService.getRegistrationsByEvent(event.getEventId());
        List<User> users = fileStorage.loadUsers();

        List<String> lines = new ArrayList<>();
        lines.add("eventId,eventTitle,studentNo,studentName,registeredAt,status");

        for (Registration registration : registrations) {
            if (!registration.isActive()) {
                continue;
            }

            String studentName = findStudentName(users, registration.getStudentNo()).orElse("");
            lines.add(CsvUtils.toLine(Arrays.asList(
                    String.valueOf(event.getEventId()),
                    event.getTitle(),
                    registration.getStudentNo(),
                    studentName,
                    registration.getRegisteredAt() == null
                            ? ""
                            : registration.getRegisteredAt().format(DATE_TIME_FORMATTER),
                    registration.getStatus()
            )));
        }

        Path exportPath = exportDirectory.resolve("event_" + event.getEventId() + "_registrations.csv");
        try {
            Files.createDirectories(exportDirectory);
            Files.write(exportPath, lines, StandardCharsets.UTF_8);
            return exportPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot export CSV: " + exportPath, exception);
        }
    }

    private Optional<String> findStudentName(List<User> users, String studentNo) {
        return users.stream()
                .filter(user -> user instanceof Student)
                .map(user -> (Student) user)
                .filter(student -> student.getStudentNo().equals(studentNo))
                .map(Student::getName)
                .findFirst();
    }
}
