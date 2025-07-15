<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="javax.naming.InitialContext, java.math.BigDecimal, com.randika.seylanbank.core.model.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('CUSTOMER')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Fund Transfer</h2>
<%
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        try {
            InitialContext ctx = new InitialContext();
            com.randika.seylanbank.core.service.TransactionService transactionService = (com.randika.seylanbank.core.service.TransactionService) ctx.lookup("java:global/ear/banking/TransactionProcessingBean!com.randika.seylanbank.core.service.TransactionService");
            com.randika.seylanbank.core.service.UserService userService = (com.randika.seylanbank.core.service.UserService) ctx.lookup("java:global/ear/auth/UserSessionBean!com.randika.seylanbank.core.service.UserService");

            Long fromAccountId = Long.parseLong(request.getParameter("fromAccountId"));
            Long toAccountId = Long.parseLong(request.getParameter("toAccountId"));
            BigDecimal amount = new BigDecimal(request.getParameter("amount"));
            User user = userService.findUserByUsername(request.getRemoteUser());

            if (user != null && user.getCustomer() != null) {
                transactionService.customerTransferFunds(fromAccountId, toAccountId, amount, user.getCustomer().getId());
                request.setAttribute("successMessage", "Funds transferred successfully!");
            } else {
                throw new Exception("User customer data not found.");
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Fund transfer failed: " + e.getMessage());
        }
    }
%>
<c:if test="${not empty errorMessage}"><p style="color:red;"><c:out value="${errorMessage}"/></p></c:if>
<c:if test="${not empty successMessage}"><p style="color:green;"><c:out value="${successMessage}"/></p></c:if>
<form method="post">
    From Account ID: <input type="text" name="fromAccountId" required><br>
    To Account ID: <input type="text" name="toAccountId" required><br>
    Amount: <input type="text" name="amount" required><br>
    <input type="submit" value="Transfer Funds">
</form>
<jsp:include page="../common/footer.jsp"/>
