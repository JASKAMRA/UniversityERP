package edu.univ.erp.service;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.util.List;

public interface StudentService {

    List<Course> GetCatalog() throws Exception;
    List<Section> GetSection(String courseId) throws Exception;
    List<edu.univ.erp.domain.Section> getTimeTable(String userId) throws Exception;
    java.io.File GenerateCSV(String userId) throws Exception;
    String GetInstNameForSec(edu.univ.erp.domain.Section section) throws Exception;
    boolean SecReg(String userId, int sectionId) throws Exception;
    List<Object[]> GetMyReg(String userId) throws Exception;
    boolean DropEnroll(int enrollmentId) throws Exception;
    java.util.List<Object[]> getGrade(String userId) throws Exception;
    
}
