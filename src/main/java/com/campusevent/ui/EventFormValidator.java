package com.campusevent.ui;

import com.campusevent.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class EventFormValidator {

    private EventFormValidator() {
    }

    static Event buildEvent(EventFormData data) {
        if (isBlank(data.title) || isBlank(data.location) || isBlank(data.eventType)) {
            throw new IllegalArgumentException("標題、地點與活動類型不可空白");
        }

        LocalDateTime startTime = buildDateTime(data.startDate, data.startTimeText, "開始時間");
        LocalDateTime endTime = buildDateTime(data.endDate, data.endTimeText, "結束時間");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }

        int capacity = parseCapacityLimit(data.capacityText);
        if (capacity < 0) {
            throw new IllegalArgumentException("報名限制人數不可小於 0");
        }

        return new Event(
                data.eventId,
                data.title.trim(),
                data.description.trim(),
                data.location.trim(),
                data.eventType.trim(),
                startTime,
                endTime,
                data.organizerName,
                capacity
        );
    }

    private static LocalDateTime buildDateTime(LocalDate date, String timeText, String fieldName) {
        String trimmedTime = timeText == null ? "" : timeText.trim();
        if (date == null || trimmedTime.isEmpty()) {
            throw new IllegalArgumentException(fieldName + "的日期與時間不可空白");
        }

        try {
            LocalTime time = LocalTime.parse(trimmedTime, DateTimeFormatter.ISO_LOCAL_TIME);
            return LocalDateTime.of(date, time);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + "的時間格式請輸入 HH:mm，例如 10:00");
        }
    }

    private static int parseCapacityLimit(String capacityText) {
        String value = capacityText == null ? "" : capacityText.trim();
        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("報名限制人數必須是 0 或正整數");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static final class EventFormData {
        private final int eventId;
        private final String title;
        private final String description;
        private final String location;
        private final String eventType;
        private final LocalDate startDate;
        private final String startTimeText;
        private final LocalDate endDate;
        private final String endTimeText;
        private final String organizerName;
        private final String capacityText;

        EventFormData(
                int eventId,
                String title,
                String description,
                String location,
                String eventType,
                LocalDate startDate,
                String startTimeText,
                LocalDate endDate,
                String endTimeText,
                String organizerName,
                String capacityText
        ) {
            this.eventId = eventId;
            this.title = title;
            this.description = description;
            this.location = location;
            this.eventType = eventType;
            this.startDate = startDate;
            this.startTimeText = startTimeText;
            this.endDate = endDate;
            this.endTimeText = endTimeText;
            this.organizerName = organizerName;
            this.capacityText = capacityText;
        }
    }
}
