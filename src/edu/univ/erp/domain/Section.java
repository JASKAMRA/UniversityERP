package edu.univ.erp.domain;
import java.util.*;
import java.time.*;


public class Section {
    private Integer section_id;
    private String semester;
    private String course_id;
    private String instructor_id;
    private Integer capacity;
    private Integer year;
    private DayOfWeek Day;
    private String Days; 
    private String StartTime;
    private String EndTime; 
    



    public Section(){}

    public Section(Integer section_id,String semester,String course_id,String instructor_id,Integer capacity,DayOfWeek Day,Integer year){
        this.section_id=section_id;
        this.semester=semester;
        this.course_id=course_id;
        this.instructor_id=instructor_id;
        this.capacity=capacity;
        this.Day=Day;
        this.year=year;

    }
    //setter function
    public void SetSectionID(Integer id){
        this.section_id=id;
    }
    public void SetCourseID(String id){
        this.course_id=id;
    }
    public void SetInstructorID(String id){
        this.instructor_id=id;
    }
    public void SetSemester(String semester){
        this.semester=semester;
    }
    public void SetCapacity(Integer capacity){
        this.capacity=capacity;
    }
    public void SetYear(Integer year){
        this.year=year;
    }
    public void SetDay(DayOfWeek Day){
        this.Day=Day;
    }
    public void SetDays(String semester){
        this.Days=semester;
    }
    public void SetStartTime(String semester){
        this.StartTime=semester;
    }
    public void SetEndTime(String semester){
        this.EndTime=semester;
    }
 

    //getter functions
    public Integer GetSectionID(){
        return section_id;
    }
    public String GetCourseID(){
        return course_id;
    }
    public String GetEndTime(){
        return EndTime;
    }
    public String GetStartTime(){
        return StartTime;
    }
    public String GetDays(){
        return Days;
    }
    public String GetInstructorID(){
        return instructor_id;
    }
    public String GetSemester(){
        return semester;
    }
    public Integer GetCapacity(){
        return capacity;
    }
    public Integer GetYear(){
        return year;
    }
    public DayOfWeek GetDay(){
        return Day;
    }
    @Override
    public int hashCode(){
        return Objects.hash(section_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(!(object instanceof Section)){
            return false;
        }
        
        Section u=(Section) object;
        return java.util.Objects.equals(section_id,u.section_id);
    }
    public String tostring(){
        String a="Section_Type{"+section_id+"("+course_id+")"+"-"+semester+"}";
        return(a);
    }
    
}
