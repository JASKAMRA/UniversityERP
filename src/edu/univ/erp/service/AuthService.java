package edu.univ.erp.service;

public interface AuthService {
    boolean changePassword(String username, String oldPassword, String newPassword) throws Exception;
}
