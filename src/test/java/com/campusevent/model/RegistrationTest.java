package com.campusevent.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationTest {

    @Test
    void activeRegistrationCanBeCancelled() {
        Registration registration = new Registration(1, "S001", 10, LocalDateTime.now(), RegistrationStatus.REGISTERED);

        assertTrue(registration.isActive());

        registration.cancel();

        assertFalse(registration.isActive());
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
    }

    @Test
    void registrationCanBeMarkedAsEventDeleted() {
        Registration registration = new Registration(1, "S001", 10, LocalDateTime.now(), RegistrationStatus.REGISTERED);

        registration.markEventDeleted();

        assertFalse(registration.isActive());
        assertEquals(RegistrationStatus.EVENT_DELETED, registration.getStatus());
    }
}
