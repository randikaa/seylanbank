package com.randika.seylanbank.reports.bean;

import com.randika.seylanbank.core.service.ReportService;
import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.reports.generator.ExcelReportGenerator;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class TransactionReportBean implements ReportService {

    private static final Logger LOGGER = Logger.getLogger(TransactionReportBean.class.getName());

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @EJB
    private TransactionService transactionService;

    @Override
    @RolesAllowed({"ADMIN"})
    public byte[] generateTransactionReport(Date fromDate, Date toDate) {
        try {
            LOGGER.info("Generating transaction report from " + fromDate + " to " + toDate);

            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :fromDate AND :toDate " +
                            "ORDER BY t.transactionDate DESC", Transaction.class);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);

            List<Transaction> transactions = query.getResultList();

            // Generate Excel report
            return ExcelReportGenerator.generateTransactionReport(transactions, fromDate, toDate);

        } catch (Exception e) {
            LOGGER.severe("Error generating transaction report: " + e.getMessage());
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
    public byte[] generateCustomerReport() {
        // This method is not implemented in this bean
        return new byte[0];
    }
}