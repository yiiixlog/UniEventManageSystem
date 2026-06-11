package com.campusevent.ui;

import com.campusevent.model.Event;

import java.time.LocalDateTime;

final class EventStatusFormatter {

    private EventStatusFormatter() {
    }

    static String registrationCount(Event event, int registeredCount) {
        return "報名 " + registeredCount + " / " + event.getCapacityText();
    }

    static String registrationCountField(Event event, int registeredCount) {
        return "報名：" + registeredCount + " / " + event.getCapacityText();
    }

    static String disabledRegisterButtonText(Event event, int registeredCount) {
        LocalDateTime now = LocalDateTime.now();
        if (event.hasCapacityLimit() && registeredCount >= event.getCapacity()) {
            return "已額滿";
        }
        if (!now.isBefore(event.getStartTime()) && now.isBefore(event.getEndTime())) {
            return "活動進行中，無法報名";
        }
        if (now.isAfter(event.getEndTime())) {
            return "活動已結束";
        }
        return "未開放報名";
    }
}
