<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.*, com.randika.seylanbank.core.model.*, javax.naming.InitialContext, java.text.SimpleDateFormat" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('CUSTOMER')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Transaction History</h2>
<form method="get">
    Account ID: <input type="text" name="accountId" value="${param.accountId}" required><br>
    From Date (yyyy-MM-dd): <input type="text" name="fromDate" value="${param.fromDate}"><br>
    To Date (yyyy-MM-dd): <input type="text" name="toDate" value="${param.toDate}"><br>
    <input type="submit" value="View History">
</form>
<hr>
<%
    if (request.getParameter("accountId") != null) {
        try {
            InitialContext ctx = new InitialContext();
            com.randika.seylanbank.core.service.TransactionService transactionService = (com.randika.seylanbank.core.service.TransactionService) ctx.lookup("java:global/ear/banking/TransactionProcessingBean!com.randika.seylanbank.core.service.TransactionService");
            com.randika.seylanbank.core.service.UserService userService = (com.randika.seylanbank.core.service.UserService) ctx.lookup("java:global/ear/auth/UserSessionBean!com.randika.seylanbank.core.service.UserService");

            Long accountId = Long.parseLong(request.getParameter("accountId"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // Default to last 30 days if dates are not provided
            Date toDate = request.getParameter("toDate") != null && !request.getParameter("toDate").isEmpty() ? sdf.parse(request.getParameter("toDate")) : new Date();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date fromDate = request.getParameter("fromDate") != null && !request.getParameter("fromDate").isEmpty() ? sdf.parse(request.getParameter("fromDate")) : cal.getTime();

            User user = userService.findUserByUsername(request.getRemoteUser());
            if (user != null && user.getCustomer() != null) {
                List<Transaction> transactions = transactionService.getMyTransactionHistory(accountId, fromDate, toDate, user.getCustomer().getId());
                request.setAttribute("transactions", transactions);
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading transaction history: " + e.getMessage());
        }
    }
%>
<c:if test="${not empty errorMessage}"><p style="color:red;"><c:out value="${errorMessage}"/></p></c:if>
<c:if test="${not empty transactions}">
    <table border="1">
        <thead><tr><th>ID</th><th>Date</th><th>Type</th><th>Description</th><th>Amount</th><th>Balance After</th></tr></thead>
        <tbody>
        <c:forEach var="tx" items="${transactions}">
            <tr>
                <td><c:out value="${tx.transactionId}"/></td>
                <td><fmt:formatDate value="${tx.transactionDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                <td><c:out value="${tx.type.displayName}"/></td>
                <td><c:out value="${tx.description}"/></td>
                <td><fmt:formatNumber value="${tx.amount}" type="currency"/></td>
                <td><fmt:formatNumber value="${tx.balanceAfter}" type="currency"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
<jsp:include page="../common/footer.jsp"/>
