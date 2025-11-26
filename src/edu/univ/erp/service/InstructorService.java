// in src/edu/univ/erp/service/InstructorService.java
package edu.univ.erp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InstructorService {
    List<Map<String,Object>> GetAssgnSec(String instructorUserId);               
    List<Map<String,Object>> GetstuInSec(int sectionId);
    List<Map<String,Object>> GetAllSec();  
    boolean Save_Grade(int enrollmentId,String Compo,BigDecimal score);
    boolean Finalize_Grade(int ID);
    boolean IsEnrollmentIn(int Enroll_ID,int sectionId);
    boolean IsInstructorIn(String InstUserID,int sectionId);    
}
