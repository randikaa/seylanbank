<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:47 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('ADMIN') && !pageContext.request.isUserInRole('SUPER_ADMIN')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Account Management</h2>
<a href="accounts?action=create">Create New Account</a>
<hr>
<h3>Existing Accounts</h3>
<p>Please use the "Account Management" link in the navigation to view the account list.</p>
<jsp:include page="../common/footer.jsp"/>
