package edu.univ.erp.domain;

public enum Role {
    STUDENT,
    INSTRUCTOR,
    ADMIN;


    public static Role fromString(String s) {
        if (s==null){ 
             return null;
        }     
        try {
            return Role.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

