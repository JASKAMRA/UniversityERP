package edu.univ.erp.ui.util;

public class CurrentSession {
    private static final CurrentSession Current_instance = new CurrentSession();
    private CurrentUser Usr;
    private boolean Mant = false;
    private CurrentSession(){}

//getter setter functions for this file

    public static CurrentSession get(){
         return Current_instance;
         }
    public CurrentUser getUsr(){ 
        return Usr;
}


    public void setUsr(CurrentUser u){
     this.Usr = u;
}
    public void SetMant(boolean a){
        this.Mant= a;
     }


//helper
    public boolean isMant(){
         return Mant; 
}
}
