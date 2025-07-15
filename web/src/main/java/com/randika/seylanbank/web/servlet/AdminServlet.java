package com.randika.seylanbank.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/customers")) {
            response.sendRedirect(request.getContextPath() + "/customer/");
        } else if (pathInfo.startsWith("/accounts")) {
            response.sendRedirect(request.getContextPath() + "/account/");
        } else if (pathInfo.startsWith("/reports")) {
            response.sendRedirect(request.getContextPath() + "/report/");
        } else if (pathInfo.startsWith("/scheduled-tasks")) {
            request.getRequestDispatcher("/admin/scheduled-tasks.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/system-settings")) {
//            request.getRequestDispatcher
        }

    }

}