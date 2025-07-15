package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.service.ReportService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@WebServlet("/report/*")
public class ReportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReportServlet.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @EJB
    private ReportService reportService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/daily-balance")) {
                handleDailyBalanceReport(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/monthly-statement")) {
                handleMonthlyStatement(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/transaction-report")) {
                handleTransactionReport(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/customer-report")) {
                handleCustomerReport(request, response);
            } else {
                request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.severe("Error generating report: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleDailyBalanceReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String dateStr = request.getParameter("date");
        Date reportDate = dateStr != null ? DATE_FORMAT.parse(dateStr) : new Date();

        byte[] reportData = reportService.generateDailyBalanceReport(reportDate);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=daily-balance-" + dateStr + ".pdf");
        response.getOutputStream().write(reportData);
    }

    private void handleMonthlyStatement(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        String monthYearStr = request.getParameter("monthYear");
        Date monthYear = DATE_FORMAT.parse(monthYearStr + "-01");

        byte[] reportData = reportService.generateMonthlyStatement(accountId, monthYear);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=statement-" + accountId + "-" + monthYearStr + ".pdf");
        response.getOutputStream().write(reportData);
    }

    private void handleTransactionReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);

        byte[] reportData = reportService.generateTransactionReport(fromDate, toDate);

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=transactions-" + fromDateStr + "-to-" + toDateStr + ".xlsx");
        response.getOutputStream().write(reportData);
    }

    private void handleCustomerReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        byte[] reportData = reportService.generateCustomerReport();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=customer-report-" + DATE_FORMAT.format(new Date()) + ".pdf");
        response.getOutputStream().write(reportData);
    }
}