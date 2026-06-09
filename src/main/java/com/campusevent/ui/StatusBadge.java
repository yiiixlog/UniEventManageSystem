package com.campusevent.ui;

import com.campusevent.model.Event;
import javafx.scene.control.Label;

public final class StatusBadge {
    private static final String GREEN_STYLE = "-fx-background-color: #d8e2dc; -fx-text-fill: #2f5f56; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String GREY_STYLE = "-fx-background-color: #eceff1; -fx-text-fill: #536064; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";

    private StatusBadge() {
    }

    public static Label create(Event event, int registeredCount) {
        String status = getDisplayStatus(event, registeredCount);
        Label badge = new Label(status);
        badge.setStyle(isOpenForRegistration(event, registeredCount) ? GREEN_STYLE : GREY_STYLE);
        return badge;
    }

    public static String getDisplayStatus(Event event, int registeredCount) {
        if (isOpenForRegistration(event, registeredCount)) {
            return "報名進行中";
        }
        return event.getStatus(registeredCount);
    }

    private static boolean isOpenForRegistration(Event event, int registeredCount) {
        return event.canRegister(registeredCount);
    }
}
