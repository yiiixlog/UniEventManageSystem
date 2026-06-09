package com.campusevent.model;

import java.time.LocalDateTime;

public class Event {
    private int eventId;
    private String title;
    private String description;
    private String location;
    private String eventType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerName;
    private int capacity;

    public Event(
            int eventId,
            String title,
            String description,
            String location,
            String eventType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String organizerName,
            int capacity
    ) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventType = eventType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerName = organizerName;
        this.capacity = capacity;
    }

    public String getStatus(int registeredCount) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endTime)) {
            return "活動已結束";
        }

        if (hasCapacityLimit() && registeredCount >= capacity) {
            return "已額滿";
        }

        if (!now.isBefore(startTime) && now.isBefore(endTime)) {
            return "活動進行中";
        }

        return "未開始";
    }

    public boolean canRegister(int registeredCount) {
        return LocalDateTime.now().isBefore(startTime)
                && (!hasCapacityLimit() || registeredCount < capacity);
    }

    public boolean hasCapacityLimit() {
        return capacity > 0;
    }

    public String getCapacityText() {
        return hasCapacityLimit() ? String.valueOf(capacity) : "無限制";
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return title + " (" + eventType + ")";
    }
}
