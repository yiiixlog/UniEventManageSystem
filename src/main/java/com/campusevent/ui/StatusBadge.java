package com.campusevent.ui;

import com.campusevent.model.Event;
import javafx.scene.control.Label;

import java.time.LocalDateTime;

public final class StatusBadge {
    private static final String GREEN_STYLE = "-fx-background-color: #d8e2dc; -fx-text-fill: #2f5f56; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String BLUE_STYLE = "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String GREY_STYLE = "-fx-background-color: #eceff1; -fx-text-fill: #536064; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";

    private StatusBadge() {
    }

    public static Label create(Event event, int registeredCount) {
        return create(event, registeredCount, false);
    }

    public static Label create(Event event, int registeredCount, boolean registeredByCurrentStudent) {
        String status = getDisplayStatus(event, registeredCount, registeredByCurrentStudent);
        Label badge = new Label(status);
        if (registeredByCurrentStudent) {
            badge.setStyle(BLUE_STYLE);
        } else {
            badge.setStyle(isOpenForRegistration(event, registeredCount) ? GREEN_STYLE : GREY_STYLE);
        }
        return badge;
    }

    public static String getDisplayStatus(Event event, int registeredCount) {
        return getDisplayStatus(event, registeredCount, false);
    }

    public static String getDisplayStatus(Event event, int registeredCount, boolean registeredByCurrentStudent) {
        if (registeredByCurrentStudent) {
            return "已報名";
        }
        if (isOpenForRegistration(event, registeredCount)) {
            return "開放報名";
        }
        if (event.hasCapacityLimit() && registeredCount >= event.getCapacity()) {
            return "已額滿";
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(event.getStartTime()) && now.isBefore(event.getEndTime())) {
            return "活動進行中";
        }
        if (now.isAfter(event.getEndTime())) {
            return "活動已結束";
        }
        return "未開放報名";
    }

    private static boolean isOpenForRegistration(Event event, int registeredCount) {
        return event.canRegister(registeredCount);
    }
}
