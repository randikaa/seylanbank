package com.randika.seylanbank.reports.bean;

import com.randika.seylanbank.core.service.ReportService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.reports.generator.PDFReportGenerator;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.RolesAllowed;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CustomerReportBean implements ReportService {

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

    @Override
    @RolesAllowed({"ADMIN"})
    public byte[] generateDailyBalanceReport(Date reportDate) {
        // This method is not implemented in this bean
        return new byte[0];
    }

    @Override
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    public byte[] generateMonthlyStatement(Long accountId, Date monthYear) {
        // This method is not implemented in this bean
        return new byte[0];
    }

    @Override
    @RolesAllowed({"ADMIN"})
    public byte[] generateTransactionReport(Date fromDate, Date toDate) {
        // This method is not implemented in this bean
        return new byte[0];
    }
}