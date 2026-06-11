package com.campusevent.model;

import java.time.LocalDateTime;

public class Registration {
    private int registrationId;
    private String studentNo;
    private int eventId;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;

    public Registration(int registrationId, String studentNo, int eventId, LocalDateTime registeredAt, String status) {
        this(registrationId, studentNo, eventId, registeredAt, RegistrationStatus.fromCode(status));
    }

    public Registration(
            int registrationId,
            String studentNo,
            int eventId,
            LocalDateTime registeredAt,
            RegistrationStatus status
    ) {
        this.registrationId = registrationId;
        this.studentNo = studentNo;
        this.eventId = eventId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
    }

    public void markEventDeleted() {
        this.status = RegistrationStatus.EVENT_DELETED;
    }

    public boolean isActive() {
        return status == RegistrationStatus.REGISTERED;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public String getStatusCode() {
        return status.getCode();
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = RegistrationStatus.fromCode(status);
    }
}
