package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.model.Customer;

import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer findCustomerById(Long id);
    void updateCustomer(Customer customer);
    List<Customer> findAllCustomers();
    void deleteCustomer(Long customerId);

    List<Customer> searchByName(String name);
    List<Customer> searchByNationalId(String nationalId);
    List<Customer> searchByEmail(String email);
    List<Customer> searchByPhoneNumber(String phoneNumber);

    // Additional methods
    Customer findCustomerByNationalId(String nationalId);
    Customer findCustomerByCustomerNumber(String customerNumber);
    List<Customer> findCustomersByName(String firstName, String lastName);
    List<Customer> findCustomersCreatedBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    void deactivateCustomer(Long customerId);
    void activateCustomer(Long customerId);
}