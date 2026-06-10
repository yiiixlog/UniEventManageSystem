package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.EventTypes;
import com.campusevent.storage.FileStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventService {
    private final FileStorage fileStorage;

    public EventService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public List<Event> getAllEvents() {
        return fileStorage.loadEvents().stream()
                .sorted(Comparator.comparing(Event::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Event> findById(int eventId) {
        return getAllEvents().stream()
                .filter(event -> event.getEventId() == eventId)
                .findFirst();
    }

    public List<Event> searchEvents(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return getAllEvents();
        }

        return getAllEvents().stream()
                .filter(event -> contains(event.getTitle(), normalized)
                        || contains(event.getDescription(), normalized)
                        || contains(event.getLocation(), normalized)
                        || contains(event.getEventType(), normalized)
                        || contains(event.getOrganizerName(), normalized))
                .collect(Collectors.toList());
    }

    public List<Event> filterEvents(LocalDate date, String eventType) {
        return getAllEvents().stream()
                .filter(event -> date == null || event.getStartTime().toLocalDate().equals(date))
                .filter(event -> eventType == null
                        || eventType.trim().isEmpty()
                        || event.getEventType().equals(eventType.trim()))
                .collect(Collectors.toList());
    }

    public List<Event> findEvents(String keyword, LocalDate date, String eventType) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedType = eventType == null ? "" : eventType.trim();

        return getAllEvents().stream()
                .filter(event -> normalized.isEmpty()
                        || contains(event.getTitle(), normalized)
                        || contains(event.getDescription(), normalized)
                        || contains(event.getLocation(), normalized)
                        || contains(event.getEventType(), normalized)
                        || contains(event.getOrganizerName(), normalized))
                .filter(event -> date == null || event.getStartTime().toLocalDate().equals(date))
                .filter(event -> normalizedType.isEmpty() || event.getEventType().equals(normalizedType))
                .collect(Collectors.toList());
    }

    public List<String> getEventTypes() {
        List<String> types = new ArrayList<>(EventTypes.getTypes());
        getAllEvents().stream()
                .map(Event::getEventType)
                .filter(type -> type != null && !type.trim().isEmpty())
                .filter(type -> !types.contains(type))
                .distinct()
                .sorted()
                .forEach(types::add);
        return types;
    }

    public void addEvent(Event event) {
        List<Event> events = fileStorage.loadEvents();
        event.setEventId(nextEventId(events));
        events.add(event);
        fileStorage.saveEvents(events);
    }

    public void updateEvent(Event updatedEvent) {
        List<Event> events = fileStorage.loadEvents();
        List<Event> updatedEvents = events.stream()
                .map(event -> event.getEventId() == updatedEvent.getEventId() ? updatedEvent : event)
                .collect(Collectors.toList());
        fileStorage.saveEvents(updatedEvents);
    }

    public void deleteEvent(int eventId) {
        List<Event> events = fileStorage.loadEvents().stream()
                .filter(event -> event.getEventId() != eventId)
                .collect(Collectors.toList());
        fileStorage.saveEvents(events);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private int nextEventId(List<Event> events) {
        return events.stream()
                .map(Event::getEventId)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
