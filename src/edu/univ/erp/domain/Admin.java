package edu.univ.erp.domain;

public class Admin {
    private int adminId;
    private String name;
    private String email;
    private String userId;   

    public Admin() {}

    // Getter function
    public int GetAdminId(){ 
        return adminId; 
    }
    public String GetUserId(){
         return userId; 
    }
    public String GetName(){
         return name; 
    }
    public String GetEmail(){ 
        return email; 
    }

    // Setter functions
    public void SetAdminId(int adminId){
         this.adminId = adminId; 
    }
    public void SetUserId(String userId){ 
        this.userId = userId; 
    }
    public void SetName(String name){ 
        this.name = name; 
    }
    public void SetEmail(String email){ 
        this.email = email; 
    }

    @Override
    public String toString() {
        return "Admin{" + "adminId=" + adminId + ", userId='" + userId + '\'' +
               ", name='" + name + '\'' + ", email='" + email + '\'' + '}';
    }
}

