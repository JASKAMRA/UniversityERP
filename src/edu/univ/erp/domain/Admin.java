package edu.univ.erp.domain;

public class Admin {
    private int adminId;
    private String userId;   // varchar(50), matches users_auth.user_id
    private String name;
    private String email;

    public Admin() {}

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Admin{" + "adminId=" + adminId + ", userId='" + userId + '\'' +
               ", name='" + name + '\'' + ", email='" + email + '\'' + '}';
    }
}

