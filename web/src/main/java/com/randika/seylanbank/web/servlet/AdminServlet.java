package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.service.CustomerService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());

    @EJB
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        LOGGER.info("Admin Path: " + pathInfo);

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        }

        else if (pathInfo.equals("/customers")) {
            handleCustomers(request, response);
        }

        else if (pathInfo.equals("/accounts")) {
            request.getRequestDispatcher("/admin/accounts.jsp").forward(request, response);
        }

        else if (pathInfo.equals("/transaction-reports")) {
            request.getRequestDispatcher("/admin/transaction-reports.jsp").forward(request, response);
        }

        else if (pathInfo.equals("/scheduled-tasks")) {
            request.getRequestDispatcher("/admin/scheduled-tasks.jsp").forward(request, response);
        }

        else if (pathInfo.equals("/system-settings")) {
            request.getRequestDispatcher("/admin/system-settings.jsp").forward(request, response);
        }

        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Customer> customers = customerService.findAllCustomers();
        request.setAttribute("customers", customers);
        request.getRequestDispatcher("/admin/customers.jsp").forward(request, response);
    }
}
