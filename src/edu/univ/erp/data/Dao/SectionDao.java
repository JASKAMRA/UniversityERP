package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Section;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;


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
   
    // public void createSection(Section s) throws SQLException {
    //     String sql = "INSERT INTO sections (course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year) " +
    //                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    //     try (Connection conn = DBConnection.getStudentConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

    //         int idx = 1;
    //         ps.setString(idx++, s.GetCourseID());
    //         ps.setString(idx++, s.GetInstructorID());

    //         // legacy single day column (to keep compatibility) - store primary day name
    //         if (s.GetDay() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetDay().name());

    //         // new CSV days column (may be null)
    //         if (s.GetDays() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetDays());

    //         // start_time, end_time as strings (HH:mm) - store as VARCHAR in DB
    //         if (s.GetStartTime() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetStartTime());

    //         if (s.GetEndTime() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetEndTime());

    //         if (s.GetCapacity() == null) ps.setNull(idx++, Types.INTEGER);
    //         else ps.setInt(idx++, s.GetCapacity());

    //         ps.setString(idx++, s.GetSemester());

    //         if (s.GetYear() == null) ps.setNull(idx++, Types.INTEGER);
    //         else ps.setInt(idx++, s.GetYear());

    //         ps.executeUpdate();
    //         try (ResultSet keys = ps.getGeneratedKeys()) {
    //             if (keys.next()) s.SetSectionID(keys.getInt(1));
    //         }
    //     }
    // }

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

   
    // public void updateSection(Section s) throws SQLException {
    //     String sql = "UPDATE sections SET course_id = ?, Instructor_id = ?, Day = ?, Days = ?, Start_time = ?, End_time = ?, " +
    //                  "Capacity = ?, Semester = ?, Year = ? where section_id = ?";
    //     try (Connection conn = DBConnection.getStudentConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql)) {

    //         int idx = 1;
    //         ps.setString(idx++, s.GetCourseID());
    //         ps.setString(idx++, s.GetInstructorID());
    //         if (s.GetDay() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetDay().name());

    //         if (s.GetDays() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetDays());

    //         if (s.GetStartTime() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetStartTime());

    //         if (s.GetEndTime() == null) ps.setNull(idx++, Types.VARCHAR);
    //         else ps.setString(idx++, s.GetEndTime());

    //         if (s.GetCapacity() == null) ps.setNull(idx++, Types.INTEGER);
    //         else ps.setInt(idx++, s.GetCapacity());

    //         ps.setString(idx++, s.GetSemester());

    //         if (s.GetYear() == null) ps.setNull(idx++, Types.INTEGER);
    //         else ps.setInt(idx++, s.GetYear());

    //         ps.setInt(idx++, s.GetSectionID());
    //         ps.executeUpdate();
    //     }
    // }

    // public void deleteSection(int sectionId) throws SQLException {
    //     String sql = "DELETE FROM sections WHERE section_id = ?";
    //     try (Connection conn = DBConnection.getStudentConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql)) {
    //         ps.setInt(1, sectionId);
    //         ps.executeUpdate();
    //     }
    // }

 
    // public java.util.List<Object[]> findCourseSummaries() throws SQLException {
    //     String sql =
    //         "SELECT DISTINCT s.course_id, c.title, c.credits, COALESCE(i.name, '') AS instructor_name " +
    //         "FROM sections s " +
    //         "JOIN courses c ON s.course_id = c.course_id " +
    //         "LEFT JOIN instructors i ON ( " +
    //         "   (i.user_id IS NOT NULL AND i.user_id = s.instructor_id) OR " +
    //         "   (CAST(i.instructor_id AS CHAR) = s.instructor_id) " +
    //         ")";
    //     java.util.List<Object[]> out = new java.util.ArrayList<>();
    //     try (Connection conn = DBConnection.getStudentConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql);
    //          ResultSet rs = ps.executeQuery()) {
    //         while (rs.next()) {
    //             String courseId = rs.getString("course_id");
    //             String title = rs.getString("title");
    //             int credits = rs.getInt("credits");
    //             String instr = rs.getString("instructor_name");
    //             out.add(new Object[] { courseId, title, credits, instr });
    //         }
    //     }
    //     return out;
    // }


    // public List<Section> getAllSections() throws SQLException {
    //     String sql = "SELECT section_id, course_id, Instructor_id, Day, Days, Start_time, End_time, Capacity, Semester, YEAR FROM sections ORDER BY course_id, section_id";
    //     List<Section> out = new ArrayList<>();
    //     try (Connection conn = DBConnection.getStudentConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql);
    //          ResultSet rs = ps.executeQuery()) {
    //         while (rs.next()) {
    //             out.add(mapRowToSection(rs));
    //         }
    //     }
    //     return out;
    // }
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
}
