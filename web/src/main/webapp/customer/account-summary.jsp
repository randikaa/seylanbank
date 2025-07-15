<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.List, com.randika.seylanbank.core.model.*, javax.naming.InitialContext" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('CUSTOMER')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Account Summary</h2>
<%
    try {
        InitialContext ctx = new InitialContext();
        com.randika.seylanbank.core.service.UserService userService = (com.randika.seylanbank.core.service.UserService) ctx.lookup("java:global/ear/auth/UserSessionBean!com.randika.seylanbank.core.service.UserService");
        com.randika.seylanbank.core.service.AccountService accountService = (com.randika.seylanbank.core.service.AccountService) ctx.lookup("java:global/ear/banking/AccountManagementBean!com.randika.seylanbank.core.service.AccountService");

        String username = request.getRemoteUser();
        User user = userService.findUserByUsername(username);
        if (user != null && user.getCustomer() != null) {
            List<Account> accounts = accountService.findMyAccounts(user.getCustomer().getId());
            request.setAttribute("accounts", accounts);
        }
    } catch (Exception e) {
        request.setAttribute("errorMessage", "Could not retrieve account summary: " + e.getMessage());
    }
%>
<c:if test="${not empty errorMessage}"><p style="color:red;"><c:out value="${errorMessage}"/></p></c:if>
<table border="1">
    <thead><tr><th>Account Number</th><th>Type</th><th>Status</th><th>Balance</th></tr></thead>
    <tbody>
    <c:forEach var="account" items="${accounts}">
        <tr>
            <td><c:out value="${account.accountNumber}"/></td>
            <td><c:out value="${account.accountType.displayName}"/></td>
            <td><c:out value="${account.status}"/></td>
            <td><fmt:formatNumber value="${account.balance}" type="currency"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<jsp:include page="../common/footer.jsp"/>
