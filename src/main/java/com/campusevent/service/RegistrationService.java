package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import com.campusevent.storage.FileStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RegistrationService {
    private final FileStorage fileStorage;

    public RegistrationService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public Registration register(Student student, Event event) {
        List<Registration> registrations = fileStorage.loadRegistrations();

        boolean alreadyRegistered = registrations.stream()
                .anyMatch(registration -> registration.getEventId() == event.getEventId()
                        && registration.getStudentNo().equals(student.getStudentNo())
                        && registration.isActive());

        if (alreadyRegistered) {
            throw new IllegalArgumentException("您已報名此活動");
        }

        int registeredCount = countActiveRegistrations(event.getEventId());
        if (!event.canRegister(registeredCount)) {
            throw new IllegalArgumentException("活動已額滿或目前不可報名");
        }

        Registration registration = new Registration(
                nextRegistrationId(registrations),
                student.getStudentNo(),
                event.getEventId(),
                LocalDateTime.now(),
                "REGISTERED"
        );
        registrations.add(registration);
        fileStorage.saveRegistrations(registrations);
        return registration;
    }

    public void cancelRegistration(int registrationId) {
        List<Registration> registrations = fileStorage.loadRegistrations();
        boolean found = false;

        for (Registration registration : registrations) {
            if (registration.getRegistrationId() == registrationId) {
                registration.cancel();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("找不到報名紀錄");
        }

        fileStorage.saveRegistrations(registrations);
    }

    public void cancelRegistrationsByEvent(int eventId) {
        List<Registration> registrations = fileStorage.loadRegistrations();
        for (Registration registration : registrations) {
            if (registration.getEventId() == eventId && registration.isActive()) {
                registration.cancel();
            }
        }
        fileStorage.saveRegistrations(registrations);
    }

    public List<Registration> getRegistrationsByStudent(String studentNo) {
        return fileStorage.loadRegistrations().stream()
                .filter(registration -> registration.getStudentNo().equals(studentNo))
                .collect(Collectors.toList());
    }

    public List<Registration> getRegistrationsByEvent(int eventId) {
        return fileStorage.loadRegistrations().stream()
                .filter(registration -> registration.getEventId() == eventId)
                .collect(Collectors.toList());
    }

    public int countActiveRegistrations(int eventId) {
        return (int) fileStorage.loadRegistrations().stream()
                .filter(registration -> registration.getEventId() == eventId)
                .filter(Registration::isActive)
                .count();
    }

    private int nextRegistrationId(List<Registration> registrations) {
        return registrations.stream()
                .map(Registration::getRegistrationId)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
