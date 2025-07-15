package com.randika.seylanbank.banking.validator;

import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.exception.InvalidTransactionException;
import com.randika.seylanbank.core.util.ValidationUtil;

import java.time.LocalDate;
import java.time.Period;
import java.util.logging.Logger;

public class CustomerValidator {

    private static final Logger LOGGER = Logger.getLogger(CustomerValidator.class.getName());
    private static final int MINIMUM_AGE = 18;

    public static void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidTransactionException("Customer cannot be null");
        }

        validatePersonalDetails(customer);
        validateContactDetails(customer);
        validateIdentificationDetails(customer);
    }

    public static void validatePersonalDetails(Customer customer) {
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new InvalidTransactionException("First name is required");
        }

        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new InvalidTransactionException("Last name is required");
        }

        if (customer.getDateOfBirth() == null) {
            throw new InvalidTransactionException("Date of birth is required");
        }

        validateAge(customer.getDateOfBirth());
    }

    public static void validateContactDetails(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new InvalidTransactionException("Email is required");
        }

        if (!ValidationUtil.isValidEmail(customer.getEmail())) {
            throw new InvalidTransactionException("Invalid email format");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            throw new InvalidTransactionException("Phone number is required");
        }

        if (!ValidationUtil.isValidPhoneNumber(customer.getPhoneNumber())) {
            throw new InvalidTransactionException("Invalid phone number format");
        }

        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new InvalidTransactionException("Address is required");
        }
    }

    public static void validateIdentificationDetails(Customer customer) {
        if (customer.getNationalId() == null || customer.getNationalId().trim().isEmpty()) {
            throw new InvalidTransactionException("National ID is required");
        }

        if (!isValidNationalId(customer.getNationalId())) {
            throw new InvalidTransactionException("Invalid National ID format");
        }
    }

    private static void validateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        int age = Period.between(dateOfBirth, currentDate).getYears();

        if (age < MINIMUM_AGE) {
            throw new InvalidTransactionException("Customer must be at least " + MINIMUM_AGE + " years old");
        }

        if (age > 120) {
            throw new InvalidTransactionException("Invalid date of birth");
        }
    }

    private static boolean isValidNationalId(String nationalId) {
        // Implementation would depend on your country's national ID format
        // This is a simple example
        return nationalId.matches("\\d{9}[vVxX]") || nationalId.matches("\\d{12}");
    }

    public static void validateCustomerUpdate(Customer existingCustomer, Customer updatedCustomer) {
        if (existingCustomer == null || updatedCustomer == null) {
            throw new InvalidTransactionException("Customers cannot be null for update");
        }

        if (!existingCustomer.getId().equals(updatedCustomer.getId())) {
            throw new InvalidTransactionException("Customer IDs must match for update");
        }

        // Validate that critical fields are not changed inappropriately
        if (!existingCustomer.getNationalId().equals(updatedCustomer.getNationalId())) {
            throw new InvalidTransactionException("National ID cannot be changed");
        }

        if (!existingCustomer.getDateOfBirth().equals(updatedCustomer.getDateOfBirth())) {
            throw new InvalidTransactionException("Date of birth cannot be changed");
        }

        validateCustomer(updatedCustomer);
    }
}