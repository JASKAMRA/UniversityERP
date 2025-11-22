package edu.univ.erp.domain;
import java.util.Objects;




public class User {
    private String user_id;
    private String username;
    private String email_id;
    private String hashed_password;
    private Role role;
    private String status = "ACTIVE";

    public User() {}

    public User(String user_id,String username,String email,String hashed_pass,Role role){
        this.user_id=user_id;
        this.email_id=email;
        this.hashed_password=hashed_pass;
        this.role=role;
        this.username=username;
    }

    // now we are adding some getter and setter functions

    //setter function
    public void SetID(String id){
        this.user_id=id;
    }
    public void SetEmail(String email){
        this.email_id=email;
    }
    public void SetUsername(String username){
        this.username=username;
    }
    public void SetHashPass(String pass){
        this.hashed_password=pass;
    }
    public void SetRole(Role role){
        this.role=role;
    }
    public void SetStatus(String status){
        this.status=status;
    }


    //getter functions
    public String GetID(){
        return(user_id);
    }
    public String GetEmail(){
        return(email_id);
    }
    public String GetHashPass(){
        return(hashed_password);
    }
    public String GetUsername(){
        return(username);
    }
    public Role GetRole(){
        return(role);
    }
    public String GetStatus(){
        return(status);
    }

    @Override
    public int hashCode(){
        return Objects.hash(user_id);  //because hashcode cannot be same
    }

    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(!(object instanceof User)){
            return false;
        } 
        User u=(User) object;
        return java.util.Objects.equals(user_id,u.user_id);
    }


}
