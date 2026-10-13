package com.university;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {

    @Test
    void shouldRegisterStudentForValidCourse() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertTrue(result);
        assertTrue(student.isRegistered("CS301"));
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectInactiveCourse() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                false,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS301"));
    }

    @Test
    void shouldRejectMissingPrerequisite() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS401",
                "Advanced Algorithms",
                4,
                30,
                true,
                "CS301"
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS401"));
    }

    @Test
    void shouldAllowRegistrationWhenPrerequisiteIsCompleted() {

        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS301");

        Course course = new Course(
                "CS401",
                "Advanced Algorithms",
                4,
                30,
                true,
                "CS301"
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertTrue(result);
        assertTrue(student.isRegistered("CS401"));
    }

    @Test
    void shouldRejectDuplicateRegistration() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        assertTrue(service.register(student, course));

        boolean secondAttempt = service.register(student, course);

        assertFalse(secondAttempt);
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectRegistrationWhenCreditLimitIsExceeded() {

        Student student = new Student("S101", "Anita");

        // Simulate 20 existing credits
        student.addRegistration("CS101", 4);
        student.addRegistration("CS102", 4);
        student.addRegistration("CS201", 4);
        student.addRegistration("CS202", 4);
        student.addRegistration("CS203", 4);

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS301"));
    }
    @Test
    void shouldRejectRegistrationWhenCourseIsAtCapacity() {

        Student student = new Student("S201", "Rahul");

        Course course = new Course(
                "CS501",
                "Operating Systems",
                4,
                30,
                true,
                null
        );

        for (int i = 0; i < 30; i++) {
            course.incrementEnrollment();
        }

        RegistrationService service = new RegistrationService();

        int creditsBefore = student.getTotalCredits();
        int enrollmentBefore = course.getCurrentEnrollment();

        boolean result = service.register(student, course);

        assertFalse(result);

        assertFalse(student.isRegistered("CS501"));
        assertEquals(creditsBefore, student.getTotalCredits());

        assertEquals(enrollmentBefore, course.getCurrentEnrollment());
        assertEquals(30, course.getCurrentEnrollment());
    }

    @Test
    void shouldAllowRegistrationWhenExactlyOneSeatRemains() {

        Student student = new Student("S202", "Priya");

        Course course = new Course(
                "CS502",
                "Database Systems",
                3,
                30,
                true,
                null
        );

        for (int i = 0; i < 29; i++) {
            course.incrementEnrollment();
        }

        RegistrationService service = new RegistrationService();

        int creditsBefore = student.getTotalCredits();
        int enrollmentBefore = course.getCurrentEnrollment();

        boolean result = service.register(student, course);


        assertTrue(result);

        assertTrue(student.isRegistered("CS502"));
        assertEquals(creditsBefore + 3, student.getTotalCredits());

        assertEquals(enrollmentBefore + 1, course.getCurrentEnrollment());
        assertEquals(30, course.getCurrentEnrollment());
    }
    
    @Test
    void shouldCreateAuditEntryForSuccessfulRegistration() {

        Student student = new Student("S301", "Anita");

        Course course = new Course(
                "CS601", "Software Engineering",
                3, 30, true, null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertTrue(result);
        assertEquals(1, service.getAuditLog().size());

        String entry = service.getAuditLog().get(0);

        assertTrue(entry.contains("S301"));
        assertTrue(entry.contains("CS601"));
    }

    @Test
    void shouldNotCreateAuditEntryForRejectedRegistration() {

        Student student = new Student("S302", "Rahul");

        Course course = new Course(
                "CS602", "Computer Networks",
                4, 30, false, null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertTrue(service.getAuditLog().isEmpty());

        assertFalse(student.isRegistered("CS602"));
        assertEquals(0, student.getTotalCredits());
        assertEquals(0, course.getCurrentEnrollment());
    }

    @Test
    void shouldPreserveAuditEntryAfterFailedDuplicateRegistration() {

        Student student = new Student("S303", "Priya");

        Course course = new Course(
                "CS603", "Database Systems",
                3, 30, true, null
        );

        RegistrationService service = new RegistrationService();

        assertTrue(service.register(student, course));

        String originalEntry = service.getAuditLog().get(0);

        assertFalse(service.register(student, course));
        assertEquals(1, service.getAuditLog().size());
        assertEquals(originalEntry, service.getAuditLog().get(0));

        assertEquals(3, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    

}