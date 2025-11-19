// in src/edu/univ/erp/service/InstructorService.java
package edu.univ.erp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InstructorService {
    List<Map<String,Object>> getAssignedSections(String instructorUserId);
    List<Map<String,Object>> getAllSections();                 // NEW
    List<Map<String,Object>> getStudentsInSection(int sectionId);
    boolean saveGrade(int enrollmentId, String component, BigDecimal score);
    boolean finalizeGrades(int sectionId);

    boolean isInstructorOfSection(String instructorUserId, int sectionId);
    boolean isEnrollmentInSection(int enrollmentId, int sectionId);
}
