package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventServiceTest {

    @TempDir
    Path dataDirectory;

    private FileStorage storage;
    private EventService service;

    @BeforeEach
    void setUp() throws Exception {
        Files.write(dataDirectory.resolve("events.csv"), Arrays.asList(
                "eventId,title,description,location,eventType,startTime,endTime,organizerName,capacity",
                "1,Design Talk,Intro,Room 101,Talk,2026-06-12T10:00:00,2026-06-12T12:00:00,Design Office,20",
                "2,AI Workshop,Practice,Room 202,Workshop,2026-06-15T09:00:00,2026-06-15T11:00:00,AI Club,15"
        ));
        storage = new FileStorage(dataDirectory);
        service = new EventService(storage);
    }

    @Test
    void getAllEventsSortsNewestStartTimeFirst() {
        List<Event> events = service.getAllEvents();

        assertEquals(2, events.size());
        assertEquals("AI Workshop", events.get(0).getTitle());
        assertEquals("Design Talk", events.get(1).getTitle());
    }

    @Test
    void findEventsCombinesKeywordDateAndTypeFilters() {
        List<Event> events = service.findEvents("ai", LocalDate.of(2026, 6, 15), "Workshop");

        assertEquals(1, events.size());
        assertEquals(2, events.get(0).getEventId());
    }

    @Test
    void addEventAssignsNextId() {
        service.addEvent(new Event(
                0,
                "Career Fair",
                "Meet companies",
                "Hall",
                "Fair",
                LocalDateTime.of(2026, 6, 20, 13, 0),
                LocalDateTime.of(2026, 6, 20, 17, 0),
                "Career Center",
                100
        ));

        List<Event> events = storage.loadEvents();

        assertEquals(3, events.size());
        assertEquals(3, events.get(2).getEventId());
        assertEquals("Career Fair", events.get(2).getTitle());
    }

    @Test
    void deleteEventRemovesMatchingEvent() {
        service.deleteEvent(1);

        List<Event> events = storage.loadEvents();

        assertEquals(1, events.size());
        assertEquals(2, events.get(0).getEventId());
    }
}
