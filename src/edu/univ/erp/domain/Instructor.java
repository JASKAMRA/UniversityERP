package edu.univ.erp.domain;
import java.util.Objects;

public class Instructor {
    private String instructor_id;
    private String user_id;
    private String email_id;
    private String name;
    private String department;

    public Instructor(){}

    public Instructor(String user_id,String instructor_id,String email,String name,String department){
        this.instructor_id=instructor_id;
        this.email_id=email;
        this.name=name;
        this.department=department;
        this.user_id=user_id;

    }

    // now we are adding some getter and setter functions

    //setter function
    public void SetID(String id){
        this.instructor_id=id;
    }
    public void SetUserID(String id){
        this.user_id=id;
    }
    public void SetEmail(String email){
        this.email_id=email;
    }
    public void Setdepartment(String department){
        this.department=department;
    }
    public void SetName(String name){
        this.name=name;
    }

    //getter functions
    public String GetID(){
        return instructor_id;
    }
    public String GetUserID(){
        return user_id;
    }
    public String GetEmail(){
        return email_id;
    }
    public String  GetDepartment(){
        return department;
    }
    public String GetName(){
        return name;
    }

    @Override
    public int hashCode(){
        return Objects.hash(user_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(object instanceof Instructor){
            Instructor u=(Instructor) object;
            return(Objects.equals(instructor_id,u.instructor_id));
        }
        else{
            return(false);
        }
    }
    public String tostring(){
        String a="Instructor_Type{"+user_id+"-"+name+"}";
        return(a);
    }


}
