package edu.univ.erp.service;
import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.CourseDao;
import edu.univ.erp.data.Dao.EnrollmentDao;
import edu.univ.erp.data.Dao.SectionDao;
import edu.univ.erp.data.Dao.StudentDAO;
import edu.univ.erp.data.Dao.GradeDao;
import edu.univ.erp.data.Dao.InstructorDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.domain.Role;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;


public class StudentServiceImpl implements StudentService {

    private final CourseDao courseDao=new CourseDao();
    private final SectionDao sectionDao=new SectionDao();
    private final EnrollmentDao enrollmentDao=new EnrollmentDao();
    private final StudentDAO studentDao=new StudentDAO();
    private final GradeDao gradeDao=new GradeDao();
    private final InstructorDAO instructorDao=new InstructorDAO();

    private static final Map<String, Integer> DAY_ORDER=new HashMap<>();
    static {
        DAY_ORDER.put("MONDAY", 1);
        DAY_ORDER.put("TUESDAY", 2);
        DAY_ORDER.put("WEDNESDAY", 3);
        DAY_ORDER.put("THURSDAY", 4);
        DAY_ORDER.put("FRIDAY", 5);
        DAY_ORDER.put("SATURDAY", 6);
        DAY_ORDER.put("SUNDAY", 7);
    }

    @Override
    public List<Section> GetSec(String courseId) throws Exception {
        return sectionDao.FindFromCourse(courseId);
    }
    @Override
    public List<Course> GetCat() throws Exception {
        return courseDao.findAll();
    }

   public boolean SecReg(String userId, int sectionId) throws Exception {
    Role role = CurrentSession.get().getUsr().GetRole();
    if (!AccessControl.isActionAllowed(role, true)) {
        throw new IllegalStateException("You cant register, System is in Mantenance.");
    }
    Student st = studentDao.findByUserId(userId);
    if (st == null) {
        System.err.println("[register] no student profile for userId=" + userId);
        return false;
    }
    String studentId = st.GetStudentID();
    try (Connection Connect = DBConnection.getStudentConnection()) {
        try {Connect.setAutoCommit(false);
            SectionDao.SectionCapacityDeadline CapDeadline=sectionDao.LocKCAPdeadline(Connect, sectionId);
            if (CapDeadline==null) {
                Connect.rollback();
                System.err.println("[register] section not found: " + sectionId);
                return false;
            }
            if (CapDeadline.RegDeadline!= null) {
                if (new Timestamp(System.currentTimeMillis()).after(CapDeadline.RegDeadline)) {
                    Connect.rollback();
                    System.err.println("Section Register deadline has been passed " + sectionId);
                    return false;
                }
            }
            int enrolledCount=enrollmentDao.num_activeEnrol(Connect, sectionId);
            if (CapDeadline.Cap != null&&enrolledCount>=CapDeadline.Cap) {
                Connect.rollback();
                System.err.println("[register] no seats left");
                return false;
            }
            if (enrollmentDao.hasduplicateEnroll(Connect, studentId, sectionId)) {
                Connect.rollback();
                System.err.println("[register] duplicate enrollment detected");
                return false;
            }
            if (enrollmentDao.insertingEnroll(Connect, studentId, sectionId, "ENROLLED") != 1) {
                Connect.rollback();
                System.err.println("[register] failed to insert enrollment");
                return false;
            }
            Connect.commit();
            return true;
        } catch (Exception exception) {
            try {Connect.rollback();} 
            catch (Exception ex1) {}
            throw exception;
        } finally {
            try { Connect.setAutoCommit(true); }
            catch (Exception ex) {}
        }}}

    @Override
    public List<Object[]> GetReg(String userId) throws Exception {
    Student Stud1=studentDao.findByUserId(userId);
    if (Stud1==null){
        return Collections.emptyList();
    }
    String studentId=Stud1.GetStudentID();
    List<Enrollment> enrollments=enrollmentDao.findByStudent(studentId);
    if (enrollments.isEmpty()||enrollments==null){ 
        return Collections.emptyList();
    }
    List<Object[]> R=new ArrayList<>();
    for (Enrollment e:enrollments) {
        Section sec=sectionDao.FindFromID(e.GetSectionID());
        if (sec==null){
            continue;
        }
        Course crs=courseDao.findById(sec.GetCourseID());
        String courseId=(crs!=null) ? crs.GetCourseID() : sec.GetCourseID();
        String DaysCSV=sec.GetDays();
        if (DaysCSV==null) {
            try {
                DaysCSV=sec.GetDay() == null ? "" : sec.GetDay().name();
            } 
            catch (Exception ignore){
                DaysCSV="";
            }
        }
        R.add(new Object[]{
            e.GetEnrollmentID(),courseId,sec.GetSectionID(),
            DaysCSV==null? "" : DaysCSV,
            sec.GetSemester(),
            e.GetStatus() == null ? "" : e.GetStatus().name()
        });
    }
    return R;
}

@Override
    public boolean looseEnroll(int enrollmentId) throws Exception {
        Role role=CurrentSession.get().getUsr().GetRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            throw new IllegalStateException("Cant drop when in Mantanence mode");
        }
        enrollmentDao.deleteEnrollment(enrollmentId); 
        return true;
    }

@Override
    public List<Section> getTimeTable(String UserID) throws Exception {
        Student Stud=studentDao.findByUserId(UserID);
        if (Stud==null) {
            return Collections.emptyList();
        }
        String student_Id=Stud.GetStudentID();
        List<Enrollment> enrolls=enrollmentDao.findByStudent(student_Id);
        if (enrolls.isEmpty()||enrolls==null){
             return Collections.emptyList();
        }
        List<Section> out=new ArrayList<>();
        for (Enrollment enroll : enrolls) {
            Section sec = sectionDao.FindFromID(enroll.GetSectionID());
            if (sec != null) {
                out.add(sec);
            }
        }

        out.sort((a, b) -> {
            String Aday = Check_string(a.GetDays());
            String bday = Check_string(b.GetDays());
            String bf = FirstDay_Csv(bday);
            String af = FirstDay_Csv(Aday);
            int ai = DAY_ORDER.getOrDefault(af.toUpperCase(), 999);
            int bi = DAY_ORDER.getOrDefault(bf.toUpperCase(), 999);
            if (ai!=bi) 
                return Integer.compare(ai, bi);
            String Ast = Check_string(a.GetStartTime());
            String Bst = Check_string(b.GetStartTime());
            return 
            Ast.compareTo(Bst);
        });
        return out;
    }

 private static String FirstDay_Csv(String csv) {
        if (csv == null) 
            return "";
        String[] parts=csv.split(",");
        if(parts.length==0)
             return "";
        return parts[0].trim().toUpperCase();
    }   
private static String Check_string(String s) {
    if (s != null) {
        return s; 
    } 
    else {
        return "";
    }
}
private String CsvEsc(String Str) {
    if (Str == null) {
        return "";
    }
    if ( Str.contains("\"") ||Str.contains(",") || Str.contains("\n")) {
        String Safe_str = Str.replace("\"", "\"\"");
        return "\"" + Safe_str + "\"";
    }
    return Str;
}

@Override
public File CsvGeneration(String userId) throws Exception {
    List<Object[]> registrations=GetReg(userId);
    File out_file = File.createTempFile("transcript_" + userId + "_", ".csv");
    try (PrintWriter printWriter = new PrintWriter(out_file)) {
        printWriter.println("Course,Section,Semester,Year,Component,Score,FinalGrade");
        for (Object[] reg : registrations) { 
            Integer enrollment_Id = (Integer) reg[0];
            String course_Id = String.valueOf(reg[1]);
            Integer section_Id = (Integer) reg[2];
            String sem = String.valueOf(reg[4]);
            List<GradeDao.GradeRow> grades = gradeDao.getGradesForEnrollment(enrollment_Id);
            
            String commonFormat="%s,%d,%s,%s";
            if (grades.isEmpty()||grades== null) {
                printWriter.printf(commonFormat + ",%s,%s,%s%n",CsvEsc(course_Id),section_Id,CsvEsc(sem), "", "", "", "");
            } 
            else{
                for(GradeDao.GradeRow gr:grades) {
                    String scoreString=gr.SCORE==null? "" :gr.SCORE.toPlainString();
                    printWriter.printf(commonFormat + ",%s,%s,%s%n",CsvEsc(course_Id),section_Id,CsvEsc(sem),"",CsvEsc(gr.Comp),scoreString,CsvEsc(gr.Final_grd)
                    );}}}}
    
    return out_file;
}

    
@Override
    public String GetInstName_sec(Section Sec) throws Exception {
    if (Sec==null) {
        return "";
    }
    String instValue=Sec.GetInstructorID();
    if (instValue.trim().isEmpty()||instValue==null){
        return "";
    } 
    return instructorDao.Find_InstnameFROMid_user(instValue);
}


@Override
    public List<Object[]> getGrade(String UserID) throws Exception {
    Student Stud=studentDao.findByUserId(UserID);
    if (Stud==null) {
        return Collections.emptyList();
    }
    String StudID=Stud.GetStudentID();
    List<Enrollment> Enroll=enrollmentDao.findByStudent(StudID);
    if (Enroll.isEmpty()||Enroll==null) {
        return Collections.emptyList();
    }
    List<Object[]> Res=new ArrayList<>();
    for (Enrollment e : Enroll){
        int EnrollmentID=e.GetEnrollmentID();
        List<GradeDao.GradeRow> g=gradeDao.getGradesForEnrollment(EnrollmentID);
        if (g.isEmpty()||g==null) {
            Section section=sectionDao.FindFromID(e.GetSectionID());
            String courseId;
            if (section!=null) {
                courseId=section.GetCourseID();
            } 
            else {
                courseId="";
            }
            Res.add(new Object[]{
                courseId,e.GetSectionID(),"",null,""});
        } 
        else {
            for (GradeDao.GradeRow grade : g) {
                Res.add(new Object[]{grade.CourseID,grade.SectionID,grade.Comp,grade.SCORE,grade.Final_grd});
            }
        }
    }
    return Res;
}


}
