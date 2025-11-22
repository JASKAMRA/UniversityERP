package edu.univ.erp.domain;
import java.util.Objects;

public class Student {
    private String user_id;
    private String student_id;
    private String email_id;
    private String name;
    private String program;
    private String Roll_num;
    private int year;
    

    public Student(){}

    public Student(String student_id,String user_id,String Roll_num,String email,String name,String program,int year){
        this.name=name;
        this.user_id=user_id;
        this.student_id=student_id;
        this.Roll_num=Roll_num;
        this.program=program;
        this.year=year;
        this.email_id=email;
    }

    // now we are adding some getter and setter functions

    //setter function
    public void SetID(String id){
        this.user_id=id;
    }
    public void SetStudentID(String id){
        this.student_id=id;
    }
    public void SetEmail(String email){
        this.email_id=email;
    }
    public void SetYear(int year){
        this.year=year;
    }
    public void SetProgram(String program){
        this.program=program;
    }
    public void SetName(String name){
        this.name=name;
    }
    public void SetRollNum(String Roll_num){
        this.Roll_num=Roll_num;
    }

    //getter functions
    public String GetID(){
        return user_id;
    }
    public String GetStudentID(){
        return student_id;
    }
    public String GetEmail(){
        return email_id;
    }
    public int GetYear(){
        return year;
    }
    public String  GetProgram(){
        return program;
    }
    public String GetName(){
        return name;
    }
    public String GetRollNum(){
        return Roll_num;
    }

    @Override
    public int hashCode(){
        return Objects.hash(user_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(!(object instanceof Student)){
            return false;
        }
        Student u = (Student) object;
        return java.util.Objects.equals(user_id, u.user_id);

}
    public String tostring(){
        String a="Student_Type{"+user_id+"-"+name+"}";
        return(a);
    }


}
