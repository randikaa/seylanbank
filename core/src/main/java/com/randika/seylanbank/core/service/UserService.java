package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.enums.UserRole;
import com.randika.seylanbank.core.exception.UnauthorizedAccessException;

import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface UserService {
    User authenticateUser(String username, String password) throws UnauthorizedAccessException;
    void createUser(User user);
    void updateUser(User user);
    void changePassword(Long userId, String newPassword);
    List<User> findUsersByRole(UserRole role);

    // Additional methods
    User findUserByUsername(String username);
    List<User> findAllActiveUsers();
    void deactivateUser(Long userId);
    void activateUser(Long userId);
    boolean isUsernameTaken(String username);
    void updateLastLogin(Long userId);
}