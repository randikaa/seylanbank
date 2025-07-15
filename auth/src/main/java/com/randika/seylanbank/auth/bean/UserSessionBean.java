package com.randika.seylanbank.auth.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;

import com.randika.seylanbank.core.service.UserService;
import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.enums.UserRole;
import com.randika.seylanbank.core.exception.UnauthorizedAccessException;
import com.randika.seylanbank.core.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserSessionBean implements UserService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    @PermitAll
    public User authenticateUser(String username, String password) throws UnauthorizedAccessException {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username AND u.active = true", User.class);
            query.setParameter("username", username);

            User user = query.getSingleResult();

            if (SecurityUtil.verifyPassword(password, user.getPassword())) {
                user.setLastLogin(LocalDateTime.now());
                em.merge(user);
                return user;
            } else {
                throw new UnauthorizedAccessException("Invalid credentials");
            }
        } catch (NoResultException e) {
            throw new UnauthorizedAccessException("User not found or inactive");
        }
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public void createUser(User user) {
        // Hash password before storing
        String hashedPassword = SecurityUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        user.setCreatedDate(LocalDateTime.now());

        em.persist(user);
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public void updateUser(User user) {
        em.merge(user);
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN", "CUSTOMER"})
    public void changePassword(Long userId, String newPassword) {
        User user = em.find(User.class, userId);
        if (user != null) {
            String hashedPassword = SecurityUtil.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            em.merge(user);
        }
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public List<User> findUsersByRole(UserRole role) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role", User.class);
        query.setParameter("role", role);
        return query.getResultList();
    }

    @Override
    @PermitAll
    public User findUserByUsername(String username) {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public List<User> findAllActiveUsers() {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.active = true ORDER BY u.createdDate DESC", User.class);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public void deactivateUser(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.setActive(false);
            em.merge(user);
        }
    }

    @Override
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public void activateUser(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.setActive(true);
            em.merge(user);
        }
    }

    @Override
    @PermitAll
    public boolean isUsernameTaken(String username) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }

    @Override
    @PermitAll
    public void updateLastLogin(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            em.merge(user);
        }
    }
}