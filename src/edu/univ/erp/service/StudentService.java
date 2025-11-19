package edu.univ.erp.service;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.util.List;

public interface StudentService {

    // ------------ Course Catalog ------------
    List<Course> getCourseCatalog() throws Exception;
    List<Section> getSectionsForCourse(String courseId) throws Exception;

    // ------------ Registration ------------
    boolean registerForSection(String userId, int sectionId) throws Exception;

    // ------------ My Registrations (for UI table) ------------
    /**
     * Returns rows as:
     * [ enrollmentId(Integer), courseId(String), sectionId(Integer), day(String), semester(String), status(String) ]
     */
    List<Object[]> getMyRegistrations(String userId) throws Exception;

    /**
     * Drop enrollment by enrollment_id (PK).
     */
    boolean dropEnrollment(int enrollmentId) throws Exception;
    java.util.List<Object[]> getGrades(String userId) throws Exception;
    /**
 * Return the list of Sections the student is registered in (for timetable view).
 * userId is the auth.user_id.
 */
    List<edu.univ.erp.domain.Section> getTimetable(String userId) throws Exception;
    // in StudentService.java
    java.io.File generateTranscriptCsv(String userId) throws Exception;

   // add near other method signatures
/**
 * Return instructor name for a given Section object.
 * Implementation will handle whether section.instructor_id stores numeric instructor_id (int)
 * or a user_id (UUID string) and fetch the instructor.name appropriately.
 */
String getInstructorNameForSection(edu.univ.erp.domain.Section section) throws Exception;



}
