package com.campusevent.storage;

import com.campusevent.model.Event;
import com.campusevent.model.Organizer;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import com.campusevent.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileStorage {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path dataDirectory;

    public FileStorage() {
        this(Paths.get("data"));
    }

    public FileStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public List<User> loadUsers() {
        Path path = dataDirectory.resolve("users.csv");
        List<String> lines = readDataLines(path);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            List<String> values = CsvUtils.parseLine(line);
            String role = get(values, 0);
            String userId = get(values, 1);
            String name = get(values, 2);
            String password = get(values, 3);
            String studentNo = get(values, 4);
            String department = get(values, 5);
            String employeeNo = get(values, 6);
            String organizationName = get(values, 7);

            if ("STUDENT".equals(role)) {
                users.add(new Student(userId, name, password, studentNo, department));
            } else if ("ORGANIZER".equals(role)) {
                users.add(new Organizer(userId, name, password, employeeNo, organizationName));
            }
        }

        return users;
    }

    public List<Event> loadEvents() {
        Path path = dataDirectory.resolve("events.csv");
        List<String> lines = readDataLines(path);
        List<Event> events = new ArrayList<>();

        for (String line : lines) {
            List<String> values = CsvUtils.parseLine(line);
            events.add(new Event(
                    parseInt(get(values, 0)),
                    get(values, 1),
                    get(values, 2),
                    get(values, 3),
                    get(values, 4),
                    parseDateTime(get(values, 5)),
                    parseDateTime(get(values, 6)),
                    get(values, 7),
                    parseInt(get(values, 8))
            ));
        }

        return events;
    }

    public void saveEvents(List<Event> events) {
        List<String> lines = new ArrayList<>();
        lines.add("eventId,title,description,location,eventType,startTime,endTime,organizerName,capacity");

        for (Event event : events) {
            lines.add(CsvUtils.toLine(Arrays.asList(
                    String.valueOf(event.getEventId()),
                    event.getTitle(),
                    event.getDescription(),
                    event.getLocation(),
                    event.getEventType(),
                    formatDateTime(event.getStartTime()),
                    formatDateTime(event.getEndTime()),
                    event.getOrganizerName(),
                    String.valueOf(event.getCapacity())
            )));
        }

        writeLines(dataDirectory.resolve("events.csv"), lines);
    }

    public List<Registration> loadRegistrations() {
        Path path = dataDirectory.resolve("registrations.csv");
        List<String> lines = readDataLines(path);
        List<Registration> registrations = new ArrayList<>();

        for (String line : lines) {
            List<String> values = CsvUtils.parseLine(line);
            registrations.add(new Registration(
                    parseInt(get(values, 0)),
                    get(values, 1),
                    parseInt(get(values, 2)),
                    parseDateTime(get(values, 3)),
                    get(values, 4)
            ));
        }

        return registrations;
    }

    public void saveRegistrations(List<Registration> registrations) {
        List<String> lines = new ArrayList<>();
        lines.add("registrationId,studentNo,eventId,registeredAt,status");

        for (Registration registration : registrations) {
            lines.add(CsvUtils.toLine(Arrays.asList(
                    String.valueOf(registration.getRegistrationId()),
                    registration.getStudentNo(),
                    String.valueOf(registration.getEventId()),
                    formatDateTime(registration.getRegisteredAt()),
                    registration.getStatus()
            )));
        }

        writeLines(dataDirectory.resolve("registrations.csv"), lines);
    }

    private List<String> readDataLines(Path path) {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.size() <= 1) {
                return Collections.emptyList();
            }
            return lines.subList(1, lines.size());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read file: " + path, exception);
        }
    }

    private void writeLines(Path path, List<String> lines) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write file: " + path, exception);
        }
    }

    private String get(List<String> values, int index) {
        return index < values.size() ? values.get(index) : "";
    }

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }
}
