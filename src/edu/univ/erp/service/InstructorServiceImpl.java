package edu.univ.erp.service;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.*;
import edu.univ.erp.domain.Section;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorServiceImpl implements InstructorService {

    private final SectionDao sectionDao=new SectionDao();
    private final EnrollmentDao enrollmentDao=new EnrollmentDao();
    private final GradeDao gradeDao=new GradeDao();



    @Override
    public List<Map<String, Object>> GetAssgnSec(String instructorUserId) {
        try {
            return sectionDao.FindSec_for_Inst(instructorUserId);
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> GetstuInSec(int sectionId) {
        List<Map<String, Object>> out=new ArrayList<>();
        try {
            List<Integer> enrollmentIds=enrollmentDao.FindEnrollUsingSec(sectionId);
            if (enrollmentIds.isEmpty()||enrollmentIds==null) {
                return out;
            }
            List<Map<String, Object>> row=enrollmentDao.FindEnrollUsingStu(sectionId);
            if (row!=null){
                out.addAll(row);
            }
            return out;
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            return out;
        }
    }
    
    @Override
    public boolean isInstructorIn(String instructorUserId, int sectionId) {
        try {
            return sectionDao.IsInstinSec(instructorUserId, sectionId);
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean Save_Grade(int enrollmentId, String component, BigDecimal score) {
        try {boolean finalExists=gradeDao.finalExistsForEnrollment(enrollmentId);
            if (finalExists){
                System.err.println("saving grade after finalizing!" + enrollmentId);
                return false;
            }
            return gradeDao.GradingInserting(enrollmentId, component, score);
        }
        catch(Exception exception){
            exception.printStackTrace();
            return false;
        }
    }

    @Override
public boolean Finalize_Grade(int sectionId) {
    final Map<String, BigDecimal> COMPONENT_MAX = new HashMap<>();
    COMPONENT_MAX.put("Assignment 1", new BigDecimal("10"));
    COMPONENT_MAX.put("Assignment 2", new BigDecimal("10"));
    COMPONENT_MAX.put("Quiz 1", new BigDecimal("10"));
    COMPONENT_MAX.put("Quiz 2", new BigDecimal("10"));
    COMPONENT_MAX.put("Midsem",   new BigDecimal("25"));
    COMPONENT_MAX.put("Endsem",   new BigDecimal("30"));
    COMPONENT_MAX.put("Attendance", new BigDecimal("5"));

    String enrollSql = "SELECT e.enrollment_id FROM enrollments e WHERE e.section_id = ?";
    String compSql = "SELECT component, score FROM grades WHERE enrollment_id = ? AND component <> 'FINAL'";
    String selectFinalSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
    String insertFinalSql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, 'FINAL', ?, ?)";
    String updateFinalSql = "UPDATE grades SET score = ?, final_grade = ? WHERE grade_id = ?";

    try (Connection connect=DBConnection.getStudentConnection();
         PreparedStatement pEnroll=connect.prepareStatement(enrollSql);
         PreparedStatement pComp=connect.prepareStatement(compSql);
         PreparedStatement pSelectFinal=connect.prepareStatement(selectFinalSql);
         PreparedStatement pInsertFinal=connect.prepareStatement(insertFinalSql);
         PreparedStatement pUpdateFinal=connect.prepareStatement(updateFinalSql)) {

        connect.setAutoCommit(false);

        pEnroll.setInt(1, sectionId);
        try (ResultSet rsEnroll=pEnroll.executeQuery()) {
            while (rsEnroll.next()) {
                int enrollmentId = rsEnroll.getInt("enrollment_id");

                pComp.setInt(1, enrollmentId);
                BigDecimal sumObtained = BigDecimal.ZERO;
                BigDecimal sumMax = BigDecimal.ZERO;
                try (ResultSet rsComp = pComp.executeQuery()) {
                    while (rsComp.next()) {
                        String comp=rsComp.getString("component");
                        BigDecimal score=rsComp.getBigDecimal("score");
                        if (score==null) continue;

                        BigDecimal max=COMPONENT_MAX.get(comp);
                        if (max==null) {
                            continue;
                        }
                        sumObtained=sumObtained.add(score);
                        sumMax=sumMax.add(max);
                    }
                }

                BigDecimal percent=BigDecimal.ZERO;
                if (sumMax.compareTo(BigDecimal.ZERO) > 0) {
                    percent=sumObtained.multiply(new BigDecimal("100")).divide(sumMax, 2, BigDecimal.ROUND_HALF_UP);
                }

                String letter=numericToLetter(percent);

                pSelectFinal.setInt(1, enrollmentId);
                try (ResultSet rsFinal = pSelectFinal.executeQuery()) {
                    if (rsFinal.next()) {
                        int gradeId = rsFinal.getInt("grade_id");
                        pUpdateFinal.setBigDecimal(1, percent);
                        pUpdateFinal.setString(2, letter);
                        pUpdateFinal.setInt(3, gradeId);
                        pUpdateFinal.executeUpdate();
                    } 
                    else {
                        pInsertFinal.setInt(1, enrollmentId);
                        pInsertFinal.setBigDecimal(2, percent);
                        pInsertFinal.setString(3, letter);
                        pInsertFinal.executeUpdate();
                    }
                }
            }
        }

        connect.commit();
        connect.setAutoCommit(true);
        return true;
    } 
    catch (SQLException exception) {
        exception.printStackTrace();
        try { /* best-effort rollback */ } 
        catch (Exception ignore) {}
        return false;
    }
}



public boolean definalizeGrades(int sectionId) {
    String findEnroll = "SELECT enrollment_id FROM enrollments WHERE section_id = ?";
    String delFinal = "DELETE FROM grades WHERE enrollment_id = ? AND component = 'FINAL'";
    try (Connection connect=DBConnection.getStudentConnection();
    PreparedStatement pFind=connect.prepareStatement(findEnroll);
    PreparedStatement pDel=connect.prepareStatement(delFinal)) {
        
        connect.setAutoCommit(false);
        pFind.setInt(1, sectionId);
        try (ResultSet resultSet = pFind.executeQuery()) {
            while (resultSet.next()) {
                int enrollmentId = resultSet.getInt("enrollment_id");
                pDel.setInt(1, enrollmentId);
                pDel.executeUpdate();
            }
        }
        connect.commit();
        try { 
            connect.setAutoCommit(true); 
        } 
        catch (Exception ignore) {}
        return true;
    } 
    catch (SQLException exception) {
        exception.printStackTrace();
        return false;
    }
}

public boolean unfinalizeGrades(int sectionId) {
    return definalizeGrades(sectionId);
}

public boolean setFinalized(int sectionId, boolean finalized) {
    if (!finalized) {
        return definalizeGrades(sectionId);
    } 
    else {
        return Finalize_Grade(sectionId);
    }
}

private String numericToLetter(BigDecimal Average) {
    if (Average==null) return "F";
    double num=Average.doubleValue();
    if (num>=90.0){
        return "A";
    }
    if (num>=80.0){
        return "B";
    }
    if (num>=70.0){
        return "C";
    }
    if (num>= 60.0){
        return "D";
    }
    return "F";
}

    @Override
    public boolean IsEnrollmentIn(int EnrollID,int SecID) {
        try {
            return enrollmentDao.isEnrollmentInSection(EnrollID, SecID);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> GetAllSec() {
        try {
            return sectionDao.Findall_usingCourseTitle();
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    }
