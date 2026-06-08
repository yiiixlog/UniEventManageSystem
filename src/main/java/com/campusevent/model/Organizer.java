package com.campusevent.model;

import java.util.List;

public class Organizer extends User {
    private String employeeNo;
    private String organizationName;

    public Organizer(String userId, String name, String password, String employeeNo, String organizationName) {
        super(userId, name, password, "ORGANIZER");
        this.employeeNo = employeeNo;
        this.organizationName = organizationName;
    }

    public void createEvent(Event event) {
        // Event creation is persisted through EventService.
    }

    public void updateEvent(Event event) {
        // Event update is persisted through EventService.
    }

    public void cancelEvent(Event event) {
        // Event deletion is persisted through EventService.
    }

    public List<Registration> viewRegistrations(List<Registration> registrations) {
        return registrations;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
