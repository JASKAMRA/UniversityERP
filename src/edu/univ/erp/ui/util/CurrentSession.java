package edu.univ.erp.ui.util;

public class CurrentSession {
    private static final CurrentSession instance = new CurrentSession();
    private CurrentUser user;
    private boolean maintenance = false;
    private CurrentSession(){}

    public static CurrentSession get(){ return instance; }
    public void setUser(CurrentUser u){ this.user = u;}
    public CurrentUser getUser(){ return user;}
    public void SetMantanence(boolean m){ this.maintenance = m; }
    public boolean isMaintenance(){ return maintenance; }
}
