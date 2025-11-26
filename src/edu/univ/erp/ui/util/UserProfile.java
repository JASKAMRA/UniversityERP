package edu.univ.erp.ui.util;

public class UserProfile {
    private String NAAM;
    private String EMAIL;

    public UserProfile(String NAAM, String EMAIL) {
        this.NAAM = NAAM;
        this.EMAIL = EMAIL;
    }

    public String getNAAM(){ 
        return NAAM; 
    }
    public String getEMAIL(){
     return EMAIL;
    }
}
