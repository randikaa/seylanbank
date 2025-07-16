package com.randika.seylanbank.core.service;

import jakarta.ejb.Remote;

import java.util.Date;

@Remote
public interface CustomerReportService {
    byte[] generateCustomerReport();
}
