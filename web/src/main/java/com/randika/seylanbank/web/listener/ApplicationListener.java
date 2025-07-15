package com.randika.seylanbank.web.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class ApplicationListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(ApplicationListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("SeylanBank Application Started");

        // Initialize application-wide settings
        sce.getServletContext().setAttribute("appName", "SeylanBank Core Banking System");
        sce.getServletContext().setAttribute("appVersion", "1.0");

        // Initialize database connection pool
        initializeConnectionPool();

        // Load system configurations
        loadSystemConfigurations();

        LOGGER.info("Application initialization completed");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("SeylanBank Application Shutting Down");

        // Cleanup resources
        cleanupResources();

        LOGGER.info("Application shutdown completed");
    }

    private void initializeConnectionPool() {
        LOGGER.info("Initializing database connection pool");
        // Connection pool initialization logic
    }

    private void loadSystemConfigurations() {
        LOGGER.info("Loading system configurations");
        // Load configurations from properties files or database
    }

    private void cleanupResources() {
        LOGGER.info("Cleaning up application resources");
        // Close database connections, threads, etc.
    }
}