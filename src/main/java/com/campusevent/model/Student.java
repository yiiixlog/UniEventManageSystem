package com.campusevent.model;

import java.util.List;

public class Student extends User {
    private String studentNo;
    private String department;

    public Student(String userId, String name, String password, String studentNo, String department) {
        super(userId, name, password, "STUDENT");
        this.studentNo = studentNo;
        this.department = department;
    }

    public Registration registerFor(Event event) {
        return new Registration(0, studentNo, event.getEventId(), null, RegistrationStatus.REGISTERED);
    }

    public void cancelRegistration(Registration registration) {
        registration.cancel();
    }

    public List<Registration> viewRegistrationHistory(List<Registration> registrations) {
        return registrations;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
