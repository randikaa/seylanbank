<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:47 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.List, com.randika.seylanbank.core.model.Customer, com.randika.seylanbank.core.service.CustomerService, javax.naming.InitialContext, com.randika.seylanbank.core.util.ValidationUtil" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('ADMIN') && !pageContext.request.isUserInRole('SUPER_ADMIN')}">
    <c:redirect url="../login.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>

<h2>Customer Management</h2>

<%
    String action = request.getParameter("action");
    String customerIdParam = request.getParameter("id");
    Long customerId = null;
    if (customerIdParam != null && !customerIdParam.isEmpty()) {
        try {
            customerId = Long.parseLong(customerIdParam);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid customer ID format.");
        }
    }

    CustomerService customerService = null;
    try {
        InitialContext ctx = new InitialContext();
        customerService = (CustomerService) ctx.lookup("java:global/ear-1.0.0/banking-1.0.0/CustomerManagementBean!com.randika.seylanbank.core.service.CustomerService");
    } catch (Exception e) {
        request.setAttribute("errorMessage", "Error connecting to CustomerService: " + e.getMessage());
    }

    if ("create".equals(action) && request.getMethod().equalsIgnoreCase("POST")) {
        try {
            if (customerService != null) {
                Customer newCustomer = new Customer();
                newCustomer.setFirstName(request.getParameter("firstName"));
                newCustomer.setLastName(request.getParameter("lastName"));
                newCustomer.setEmail(request.getParameter("email"));
                newCustomer.setPhoneNumber(request.getParameter("phoneNumber"));
                newCustomer.setAddress(request.getParameter("address"));
                newCustomer.setNationalId(request.getParameter("nationalId"));
                String dobString = request.getParameter("dateOfBirth");
                LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ISO_LOCAL_DATE);
                newCustomer.setDateOfBirth(dateOfBirth);
                ;

                // VALIDATE - add ValidationUtil as you see fit

                customerService.createCustomer(newCustomer);
                response.sendRedirect("customer-management.jsp?action=list");
            } else {
                request.setAttribute("errorMessage", "CustomerService is not available.");
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Failed to create customer: " + e.getMessage());
        }
    } else if ("edit".equals(action) && request.getMethod().equalsIgnoreCase("POST") && customerId != null) {
        try {
            if (customerService != null) {
                Customer existingCustomer = customerService.findCustomerById(customerId);
                if (existingCustomer != null) {

                    existingCustomer.setFirstName(request.getParameter("firstName"));
                    existingCustomer.setLastName(request.getParameter("lastName"));
                    existingCustomer.setEmail(request.getParameter("email"));
                    existingCustomer.setPhoneNumber(request.getParameter("phoneNumber"));
                    existingCustomer.setAddress(request.getParameter("address"));
                    existingCustomer.setNationalId(request.getParameter("nationalId"));
                    String dobString = request.getParameter("dateOfBirth");
                    LocalDate dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
                    existingCustomer.setDateOfBirth(dateOfBirth);

                    customerService.updateCustomer(existingCustomer);
                    response.sendRedirect("customer-management.jsp?action=list");
                } else {
                    request.setAttribute("errorMessage", "Customer not found with ID: " + customerId);
                }
            } else {
                request.setAttribute("errorMessage", "CustomerService is not available.");
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Failed to edit customer: " + e.getMessage());
        }
    } else if ("delete".equals(action) && customerId != null) {
        try {
            if (customerService != null) {
                customerService.deleteCustomer(customerId);
                response.sendRedirect("customer-management.jsp?action=list");
            } else {
                request.setAttribute("errorMessage", "CustomerService is not available.");
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Failed to delete customer: " + e.getMessage());
        }
    }
%>

<c:if test="${not empty errorMessage}">
    <p style="color:red"><c:out value="${errorMessage}"/></p>
</c:if>

<c:choose>
    <c:when test="${action == 'create' || action == 'edit'}">
        <%-- Display the Create/Edit form --%>
        <%
            Customer customer = null;
            if ("edit".equals(action) && customerId != null && customerService != null) {
                customer = customerService.findCustomerById(customerId);
                if (customer == null) {
                    request.setAttribute("errorMessage", "Customer not found with ID: " + customerId);
                }
            }
        %>

        <c:if test="${not empty errorMessage}">
            <p  style="color:red"> <c:out value="${errorMessage}"/> </p>
        </c:if>

        <form method="post" action="customer-management.jsp?action=<c:out value="${(empty customer) ? 'create' : 'edit&id=' += customer.id}"/>">

            <label for="firstName">First Name:</label><br>
            <input type="text" id="firstName" name="firstName" required value="<c:out value="${customer.firstName}"/>"><br><br>

            <label for="lastName">Last Name:</label><br>
            <input type="text" id="lastName" name="lastName" required value="<c:out value="${customer.lastName}"/>"><br><br>

            <label for="email">Email:</label><br>
            <input type="email" id="email" name="email" required value="<c:out value="${customer.email}"/>"><br><br>

            <label for="phoneNumber">Phone Number:</label><br>
            <input type="text" id="phoneNumber" name="phoneNumber" required value="<c:out value="${customer.phoneNumber}"/>"><br><br>

            <label for="dateOfBirth">Date Of Birth:</label><br>
            <input type="date" id="dateOfBirth" name="dateOfBirth" required value="<c:out value="${customer.dateOfBirth}"/>"><br><br>

            <label for="address">Address:</label><br>
            <textarea id="address" name="address" rows="4" cols="50"><c:out value="${customer.address}"/></textarea><br><br>

            <label for="nationalId">National ID:</label><br>
            <input type="text" id="nationalId" name="nationalId" required value="<c:out value="${customer.nationalId}"/>" <c:if test="${not empty customer.id}">readonly</c:if>><br><br>

            <input type="submit" value="<c:out value="${(empty customer) ? 'Create Customer' : 'Update Customer'}"/>">
        </form>

    </c:when>
    <c:otherwise>
        <%-- List of Customers --%>
        <a href="customer-management.jsp?action=create">Create New Customer</a><br><br>

        <%
            try {
                InitialContext ctx = new InitialContext();
                CustomerService customerService1 = (CustomerService) ctx.lookup("java:global/ear-1.0.0/banking-1.0.0/CustomerManagementBean!com.randika.seylanbank.core.service.CustomerService");
                List<Customer> customers = customerService1.findAllCustomers();
                request.setAttribute("customers", customers);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Error retrieving customers: " + e.getMessage());
            }
        %>

        <c:if test="${not empty errorMessage}">
            <p style="color:red"> ${errorMessage} </p>
        </c:if>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Email</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="customer" items="${customers}">
                <tr>
                    <td><c:out value="${customer.id}"/></td>
                    <td><c:out value="${customer.firstName}"/></td>
                    <td><c:out value="${customer.lastName}"/></td>
                    <td><c:out value="${customer.email}"/></td>
                    <td>
                        <a href="customer-management.jsp?action=edit&id=${customer.id}">Edit</a> |
                        <a href="customer-management.jsp?action=delete&id=${customer.id}">Delete</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<jsp:include page="../common/footer.jsp"/>
