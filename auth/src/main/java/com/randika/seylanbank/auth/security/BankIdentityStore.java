package com.randika.seylanbank.auth.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.util.SecurityUtil;

import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;

@ApplicationScoped
public class BankIdentityStore implements IdentityStore {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            UsernamePasswordCredential usernamePassword = (UsernamePasswordCredential) credential;
            return validate(usernamePassword.getCaller(), usernamePassword.getPasswordAsString());
        }
        return CredentialValidationResult.INVALID_RESULT;
    }

    public CredentialValidationResult validate(String username, String password) {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            User user = query.getSingleResult();

            // Verify password (in production, use proper hashing)
            String hashedPassword = SecurityUtil.hashPassword(password);
            if (user.getPassword().equals(hashedPassword)) {
                Set<String> roles = new HashSet<>();
                roles.add(user.getRole().name());
                return new CredentialValidationResult(username, roles);
            }
        } catch (NoResultException e) {
            // User not found
        }
        return CredentialValidationResult.INVALID_RESULT;
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(ValidationType.VALIDATE);
    }
}