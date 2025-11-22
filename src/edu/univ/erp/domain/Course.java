package edu.univ.erp.domain;
import java.util.Objects;

public class Course {
    private String department_id;
    private Integer credits;
    private String course_id;
    private String title;

    public Course(){}

    public Course(String department_id,String title,String course_id,Integer credits){
        this.department_id=department_id;
        this.title=title;
        this.course_id=course_id;
        this.credits=credits;
    }

    // now we are adding some getter and setter functions

    //setter function
    public void SetDepartmentID(String id){
        this.department_id=id;
    }
    public void SetTitle(String title){
        this.title=title;
    }
    public void SetCourseID(String course_id){
        this.course_id=course_id;
    }
    public void SetCredits(Integer credits){
        this.credits=credits;
    }

    //getter functions
    public String GetDepartmentID(){
        return department_id;
    }
    public String GetTitle(){
        return title;
    }
    public String GetCourseID(){
        return course_id;
    }
    public Integer GetCredits(){
        return credits;
    }
    

    @Override
    public int hashCode(){
        return Objects.hash(course_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(!(object instanceof Course)){
            return false;
        }
        Course u=(Course) object;
        return(Objects.equals(course_id,u.course_id));
    }
    public String tostring(){
        String a="Course_Type{"+course_id+" "+title+"}";
        return(a);
    }


}

