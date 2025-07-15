<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="com.randika.seylanbank.core.model.Customer, com.randika.seylanbank.core.service.CustomerService, javax.naming.InitialContext, com.randika.seylanbank.core.model.User" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('CUSTOMER')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>My Profile</h2>
<%
    try {
        InitialContext ctx = new InitialContext();
        com.randika.seylanbank.core.service.UserService userService = (com.randika.seylanbank.core.service.UserService) ctx.lookup("java:global/ear/auth/UserSessionBean!com.randika.seylanbank.core.service.UserService");
        User user = userService.findUserByUsername(request.getRemoteUser());
        if (user != null && user.getCustomer() != null) {
            request.setAttribute("customer", user.getCustomer());
        } else {
            request.setAttribute("errorMessage", "Could not find customer profile data.");
        }
    } catch (Exception e) {
        request.setAttribute("errorMessage", "Error loading profile: " + e.getMessage());
    }
%>
<c:if test="${not empty errorMessage}"><p style="color:red;"><c:out value="${errorMessage}"/></p></c:if>
<c:if test="${not empty customer}">
    <p><b>Name:</b> <c:out value="${customer.firstName} ${customer.lastName}"/></p>
    <p><b>National ID:</b> <c:out value="${customer.nationalId}"/></p>
    <p><b>Phone Number:</b> <c:out value="${customer.phoneNumber}"/></p>
    <p><b>Address:</b> <c:out value="${customer.address}"/></p>
</c:if>
<jsp:include page="../common/footer.jsp"/>
