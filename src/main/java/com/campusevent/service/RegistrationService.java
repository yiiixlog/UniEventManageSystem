package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import com.campusevent.storage.FileStorage;

import java.time.LocalDateTime;
import java.util.Comparator;
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
            throw new IllegalArgumentException(getRegistrationUnavailableReason(event, registeredCount));
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

    public void markRegistrationsDeletedByEvent(int eventId) {
        List<Registration> registrations = fileStorage.loadRegistrations();
        for (Registration registration : registrations) {
            if (registration.getEventId() == eventId && registration.isActive()) {
                registration.markEventDeleted();
            }
        }
        fileStorage.saveRegistrations(registrations);
    }

    public List<Registration> getRegistrationsByStudent(String studentNo) {
        return fileStorage.loadRegistrations().stream()
                .filter(registration -> registration.getStudentNo().equals(studentNo))
                .collect(Collectors.toList());
    }

    public List<Registration> getRecentRegistrationsByStudent(String studentNo, int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        return getRegistrationsByStudent(studentNo).stream()
                .filter(registration -> registration.getRegisteredAt() != null)
                .filter(registration -> !registration.getRegisteredAt().isBefore(cutoffTime))
                .sorted(Comparator.comparing(Registration::getRegisteredAt).reversed())
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

    public boolean hasActiveRegistration(String studentNo, int eventId) {
        return fileStorage.loadRegistrations().stream()
                .anyMatch(registration -> registration.getEventId() == eventId
                        && registration.getStudentNo().equals(studentNo)
                        && registration.isActive());
    }

    private String getRegistrationUnavailableReason(Event event, int registeredCount) {
        LocalDateTime now = LocalDateTime.now();
        if (event.hasCapacityLimit() && registeredCount >= event.getCapacity()) {
            return "活動已額滿";
        }
        if (!now.isBefore(event.getStartTime()) && now.isBefore(event.getEndTime())) {
            return "活動已開始，無法報名";
        }
        if (now.isAfter(event.getEndTime())) {
            return "活動已結束，無法報名";
        }
        return "活動尚未開放報名";
    }

    private int nextRegistrationId(List<Registration> registrations) {
        return registrations.stream()
                .map(Registration::getRegistrationId)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
