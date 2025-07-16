package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import jakarta.annotation.security.RolesAllowed;

import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Customer;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CustomerManagementBean implements CustomerService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public Customer createCustomer(Customer customer) {
        customer.setCreatedDate(LocalDateTime.now());
        em.persist(customer);
        return customer;
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public Customer findCustomerById(Long id) {
        return em.find(Customer.class, id);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public void updateCustomer(Customer customer) {
        em.merge(customer);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> findAllCustomers() {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c ORDER BY c.createdDate DESC", Customer.class);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN"})
    public void deleteCustomer(Long customerId) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer != null) {
            em.remove(customer);
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public Customer findCustomerByNationalId(String nationalId) {
        try {
            TypedQuery<Customer> query = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.nationalId = :nationalId", Customer.class);
            query.setParameter("nationalId", nationalId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public Customer findCustomerByCustomerNumber(String customerNumber) {
        try {
            TypedQuery<Customer> query = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.customerNumber = :customerNumber", Customer.class);
            query.setParameter("customerNumber", customerNumber);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> findCustomersByName(String firstName, String lastName) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.firstName LIKE :firstName " +
                        "AND c.lastName LIKE :lastName", Customer.class);
        query.setParameter("firstName", "%" + firstName + "%");
        query.setParameter("lastName", "%" + lastName + "%");
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> findCustomersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.createdDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY c.createdDate DESC", Customer.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void deactivateCustomer(Long customerId) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer != null && customer.getUser() != null) {
            customer.getUser().setActive(false);
            em.merge(customer);
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void activateCustomer(Long customerId) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer != null && customer.getUser() != null) {
            customer.getUser().setActive(true);
            em.merge(customer);
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> searchByName(String name) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE :name", Customer.class);
        query.setParameter("name", "%" + name.toLowerCase() + "%");
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> searchByNationalId(String nationalId) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.nationalId LIKE :nid", Customer.class);
        query.setParameter("nid", "%" + nationalId + "%");
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> searchByEmail(String email) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.email) LIKE :email", Customer.class);
        query.setParameter("email", "%" + email.toLowerCase() + "%");
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Customer> searchByPhoneNumber(String phoneNumber) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.phoneNumber LIKE :phone", Customer.class);
        query.setParameter("phone", "%" + phoneNumber + "%");
        return query.getResultList();
    }

}