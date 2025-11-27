package edu.univ.erp.service;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.util.List;

public interface StudentService {

    List<Course> GetCat() throws Exception;
    List<Section> GetSec(String courseId) throws Exception;
    List<edu.univ.erp.domain.Section> getTimeTable(String userId) throws Exception;
    java.io.File CsvGeneration(String userId) throws Exception;
    String GetInstName_sec(edu.univ.erp.domain.Section section) throws Exception;
    boolean SecReg(String userId, int sectionId) throws Exception;
    List<Object[]> GetReg(String userId) throws Exception;
    boolean looseEnroll(int enrollmentId) throws Exception;
    java.util.List<Object[]> getGrade(String userId) throws Exception;
    
}
