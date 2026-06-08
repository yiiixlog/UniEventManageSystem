package com.campusevent.model;

import java.time.LocalDateTime;

public class Registration {
    private int registrationId;
    private String studentNo;
    private int eventId;
    private LocalDateTime registeredAt;
    private String status;

    public Registration(int registrationId, String studentNo, int eventId, LocalDateTime registeredAt, String status) {
        this.registrationId = registrationId;
        this.studentNo = studentNo;
        this.eventId = eventId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public boolean isActive() {
        return "REGISTERED".equals(status);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
