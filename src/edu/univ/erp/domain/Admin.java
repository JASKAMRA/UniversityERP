package edu.univ.erp.domain;

public class Admin {
    private int adminId;
    private String name;
    private String email;
    private String userId;   

    public Admin() {}

    // Getter function
    public int getAdminId(){ 
        return adminId; 
    }
    public String getUserId(){
         return userId; 
    }
    public String getName(){
         return name; 
    }
    public String getEmail(){ 
        return email; 
    }

    // Setter functions
    public void setAdminId(int adminId){
         this.adminId = adminId; 
    }
    public void setUserId(String userId){ 
        this.userId = userId; 
    }
    public void setName(String name){ 
        this.name = name; 
    }
    public void setEmail(String email){ 
        this.email = email; 
    }

    @Override
    public String toString() {
        return "Admin{" + "adminId=" + adminId + ", userId='" + userId + '\'' +
               ", name='" + name + '\'' + ", email='" + email + '\'' + '}';
    }
}

