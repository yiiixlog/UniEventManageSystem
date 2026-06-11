package com.campusevent.ui;

import com.campusevent.model.Registration;
import com.campusevent.model.RegistrationStatus;

final class RegistrationStatusFormatter {

    private RegistrationStatusFormatter() {
    }

    static String displayStatus(RegistrationStatus status) {
        return status.getDisplayText();
    }

    static String inactiveRegistrationMessage(Registration registration) {
        if (registration.getStatus() == RegistrationStatus.EVENT_DELETED) {
            return "活動已刪除，無法取消報名";
        }
        return "這筆報名已取消，無需重複取消";
    }
}
