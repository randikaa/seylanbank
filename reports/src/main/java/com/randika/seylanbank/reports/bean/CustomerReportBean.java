package com.randika.seylanbank.reports.bean;

import com.randika.seylanbank.core.service.CustomerReportService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.reports.generator.PDFReportGenerator;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CustomerReportBean implements CustomerReportService {

    private static final Logger LOGGER = Logger.getLogger(CustomerReportBean.class.getName());

    @EJB
    private CustomerService customerService;

    @Override
    @RolesAllowed({"ADMIN"})
    public byte[] generateCustomerReport() {
        try {
            LOGGER.info("Generating customer report");

            List<Customer> customers = customerService.findAllCustomers();

            // Generate PDF report
            return PDFReportGenerator.generateCustomerReport(customers);

        } catch (Exception e) {
            LOGGER.severe("Error generating customer report: " + e.getMessage());
            return new byte[0];
        }
    }
}