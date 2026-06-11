package com.campusevent.model;

public enum RegistrationStatus {
    REGISTERED("REGISTERED", "已報名"),
    CANCELLED("CANCELLED", "已取消"),
    EVENT_DELETED("EVENT_DELETED", "活動已刪除");

    private final String code;
    private final String displayText;

    RegistrationStatus(String code, String displayText) {
        this.code = code;
        this.displayText = displayText;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static RegistrationStatus fromCode(String code) {
        for (RegistrationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown registration status: " + code);
    }
}
