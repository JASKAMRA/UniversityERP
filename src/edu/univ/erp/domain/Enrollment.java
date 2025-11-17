package edu.univ.erp.domain;
import java.util.Objects;

public class Enrollment {
    private Integer enrollment_id;
    private Status status;
    private String student_id;
    private int section_id;
    public Enrollment(){}

    public Enrollment(Integer enrollment_id,String student_id,int section_id,Status status){
        this.enrollment_id=enrollment_id;
        this.status=status;
        this.student_id=student_id;
        this.section_id=section_id;
    }

    // now we are adding some getter and setter functions

    //setter function
    public void SetEnrollmentID(Integer id){
        this.enrollment_id=id;
    }
    public void SetStatus(Status status){
        this.status=status;
    }
    public void SetStudentID(String id){
        this.student_id=id;
    }
    public void SetSectionID(int id){
        this.section_id=id;
    }
    //getter function
    public Integer GetEnrollmentID(){
        return(enrollment_id);
    }
    public Status GetStatus(){
        return(status);
    }
    public String GetStudentID(){
        return(student_id);
    }
    public int GetSectionID(){
        return(section_id);
    }

    @Override
    public int hashCode(){
        return Objects.hash(enrollment_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(object instanceof Enrollment){
            Enrollment u=(Enrollment) object;
            return(Objects.equals(enrollment_id,u.enrollment_id));
        }
        else{
            return(false);
        }
    }
}
