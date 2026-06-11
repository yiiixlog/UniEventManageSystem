package com.campusevent.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTest {

    @Test
    void canRegisterBeforeStartWhenCapacityIsAvailable() {
        Event event = new Event(
                1,
                "Future Event",
                "Description",
                "Room 101",
                "Workshop",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Campus Office",
                2
        );

        assertTrue(event.canRegister(0));
        assertTrue(event.canRegister(1));
    }

    @Test
    void cannotRegisterWhenCapacityIsFullOrEventStarted() {
        Event fullEvent = new Event(
                1,
                "Full Event",
                "Description",
                "Room 101",
                "Workshop",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Campus Office",
                1
        );
        Event startedEvent = new Event(
                2,
                "Started Event",
                "Description",
                "Room 102",
                "Workshop",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                "Campus Office",
                0
        );

        assertFalse(fullEvent.canRegister(1));
        assertFalse(startedEvent.canRegister(0));
    }

    @Test
    void zeroCapacityMeansNoCapacityLimit() {
        Event event = new Event(
                1,
                "Unlimited Event",
                "Description",
                "Room 101",
                "Workshop",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Campus Office",
                0
        );

        assertFalse(event.hasCapacityLimit());
        assertTrue(event.canRegister(500));
    }
}
