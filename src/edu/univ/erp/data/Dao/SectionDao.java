package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Section;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SectionDao {
    public void setStringg(PreparedStatement p,String s,int i)throws SQLException{
             p.setString(i, s);
        }  
    public void setINT(PreparedStatement p,int s,int i)throws SQLException{
             p.setInt(i, s);
        }  
    public void executeUpdate(PreparedStatement p)throws SQLException{
             p.executeUpdate();
        }
    
    public int createSection(Connection conn, String courseId, int instructorId, String day, int capacity, String semester, int year) throws SQLException {
    String insertSection="INSERT INTO sections (course_id, instructor_id, day, capacity, semester, year, registration_deadline) VALUES (?, ?, ?, ?, ?, ?, NULL)";
    try (PreparedStatement prepStatement= conn.prepareStatement(insertSection, Statement.RETURN_GENERATED_KEYS)) {
        int idx = 1;
        prepStatement.setString(idx++, courseId);
        prepStatement.setInt(idx++, instructorId);
        prepStatement.setString(idx++, day);
        prepStatement.setInt(idx++, capacity);
        prepStatement.setString(idx++, semester);
        prepStatement.setInt(idx++, year);
        int result=prepStatement.executeUpdate();
        if (result!=1){
            return -1;
        }
        try (ResultSet keys=prepStatement.getGeneratedKeys()) {
            if (keys.next()){
                return keys.getInt(1);
            }
        }return -1;
    }
}
public List<Map<String,Object>> FindSec_for_Inst(String instructorUserId) throws SQLException {
    String sql ="SELECT s.section_id, s.course_id, c.title AS course_title, s.day, s.capacity, s.semester, s.year " +"FROM sections s " +"JOIN courses c ON s.course_id = c.course_id " +"JOIN instructors i ON s.instructor_id = i.instructor_id " +"WHERE i.user_id = ?";
    List<Map<String,Object>> out=new ArrayList<>();
    try (Connection Connect = DBConnection.getStudentConnection();
         PreparedStatement PrepStatement = Connect.prepareStatement(sql)) {
        setStringg(PrepStatement,instructorUserId,1);
        try (ResultSet ResultSet=PrepStatement.executeQuery()) {
            while (ResultSet.next()) {
                Map<String,Object> Map=new HashMap<>();
                Map.put("section_id", ResultSet.getInt("section_id"));Map.put("course_id", ResultSet.getString("course_id"));Map.put("course_title", ResultSet.getString("course_title"));
                Map.put("day", ResultSet.getString("day"));Map.put("capacity", ResultSet.getInt("capacity"));Map.put("semester", ResultSet.getString("semester"));Map.put("year", ResultSet.getInt("year"));
                out.add(Map);
            }
        }
    }
    return out;
}

public boolean IsInstinSec(String instructorUserId, int sectionId) throws SQLException {
    String sql="select 1 FROM sections s JOIN instructors i ON s.instructor_id = i.instructor_id WHERE s.section_id = ? AND i.user_id = ? LIMIT 1";
    try (Connection connect=DBConnection.getStudentConnection();
        PreparedStatement PrepStatement=connect.prepareStatement(sql)) {
        setINT(PrepStatement, sectionId, 1);
        setStringg(PrepStatement, instructorUserId, 2);
        try (ResultSet rs = PrepStatement.executeQuery()) {
            return rs.next();
        }
    }
}


public List<Map<String,Object>> Findall_usingCourseTitle() throws SQLException {
    String sql="SELECT s.section_id, s.course_id, c.title AS course_title, s.day, s.semester, s.year, s.capacity " +"FROM sections s LEFT JOIN courses c ON s.course_id = c.course_id ORDER BY s.course_id, s.section_id";
    List<Map<String,Object>> out=new ArrayList<>();
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement prepStatement=Connect.prepareStatement(sql);
         ResultSet ResultSet=prepStatement.executeQuery()) {
         while (ResultSet.next()) {
            Map<String,Object> map=new HashMap<>();
            map.put("section_id", ResultSet.getInt("section_id"));map.put("course_id", ResultSet.getString("course_id"));map.put("course_title", ResultSet.getString("course_title"));
            map.put("day", ResultSet.getString("day"));map.put("semester", ResultSet.getString("semester"));map.put("year", ResultSet.getInt("year"));map.put("capacity", ResultSet.getInt("capacity"));
            out.add(map);
        }}return out;
}

   

    public List<Section> FindFromCourse(String courseId) throws SQLException {
        String sql = "select section_id, Course_id, Instructor_id, Day, Days, Start_time, End_time, Capacity, Semester, Year " + "from sections WHERE course_id = ?";
        List<Section> out=new ArrayList<>();
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
             setStringg(prepStatement,courseId,1);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                while (resultSet.next()) {
                    out.add(Mapping_To_Section(resultSet));
                }}}return out;
    }

    public Section FindFromID(int id) throws SQLException {
        String sql="Select section_id, Course_id, Instructor_id, Day, Days, Start_time, End_time, Capacity, Semester, Year " + "from sections WHERE section_id = ?";
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
                setINT(prepStatement,id,1);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    return(Mapping_To_Section(resultSet));
                }
            }
        }
        return null;
    }

   
    
     private Section Mapping_To_Section(ResultSet resultSet) throws SQLException {
        Section new_s=new Section();
        new_s.SetSectionID(resultSet.getInt("section_id"));
        new_s.SetCourseID(resultSet.getString("course_id"));
        new_s.SetInstructorID(resultSet.getString("instructor_id"));
        
//changing inday
        String Day=resultSet.getString("day");
        if (Day!=null&&!Day.isEmpty()) {
            try {
                new_s.SetDay(DayOfWeek.valueOf(Day));
            } catch (IllegalArgumentException iae) {  
            }
        }    
        String Days_csv=resultSet.getString("days");
        if(Days_csv==null || Days_csv.isEmpty()){
             if (new_s.GetDay()==null) {
                new_s.SetDays(null);   
            } else {
                new_s.SetDays(new_s.GetDay().name());
            }
        }else {          
            new_s.SetDays(Days_csv);
        }
    
        String End_T=resultSet.getString("end_time");
        String Start_T=resultSet.getString("start_time");
        new_s.SetEndTime(End_T);
        new_s.SetStartTime(Start_T);
        
        int CAPAC=resultSet.getInt("capacity");
        if (!resultSet.wasNull()){
            new_s.SetCapacity(CAPAC);
            new_s.SetSemester(resultSet.getString("semester"));    
        }
        else{
            new_s.SetCapacity(null);
        }
        
//year changes
        int a = resultSet.getInt("year");
        if (!resultSet.wasNull()){
            new_s.SetYear(a);    
        }
        else{
            new_s.SetYear(null);
        }
        
    return new_s;
    }

public static class SectionCapacityDeadline {
    public final Integer Cap;
    public final java.sql.Timestamp RegDeadline;
    public SectionCapacityDeadline(Integer capacity, java.sql.Timestamp a){
         this.Cap = capacity; 
         this.RegDeadline = a; }
}

public SectionCapacityDeadline LocKCAPdeadline(Connection Connect, int SecID) throws SQLException {
    String sql="SELECT capacity, registration_deadline FROM sections WHERE section_id = ? FOR UPDATE";
    try (PreparedStatement Prepstatement=Connect.prepareStatement(sql)) {
        setINT(Prepstatement, SecID,1);
        try (ResultSet resultSet = Prepstatement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            java.sql.Timestamp RegDeadline = resultSet.getTimestamp("registration_deadline");
            int Rcap=resultSet.getInt("capacity");
            Integer Cap;
                if (resultSet.wasNull()){
                    Cap=null;
                }else {
                    Cap=Rcap;
                }
            return new SectionCapacityDeadline(Cap, RegDeadline);
        }
    }
}
}
