package edu.univ.erp.domain;

public class Course {
    private int id;
    private String code;
    private String title;
    private int credits;
    // getters/setters
    public int getId(){return id;}
    public void setId(int id){this.id=id;}
    public String getCode(){return code;}
    public void setCode(String code){this.code=code;}
    public String getTitle(){return title;}
    public void setTitle(String title){this.title=title;}
    public int getCredits(){return credits;}
    public void setCredits(int credits){this.credits=credits;}
}
