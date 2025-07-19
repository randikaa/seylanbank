<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    if (request.getUserPrincipal() != null) {
        if (request.isUserInRole("SUPER_ADMIN") || request.isUserInRole("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            return;
        } else if (request.isUserInRole("CUSTOMER")) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard.jsp");
            return;
        }
    }
%>

<jsp:include page="/common/header.jsp" />
<jsp:include page="/common/navigation.jsp" />

<h2>Login</h2>

<c:if test="${not empty errorMessage}">
    <p style="color:red;"><c:out value="${errorMessage}" /></p>
</c:if>

<form action="${pageContext.request.contextPath}/login" method="post">
    <label for="username">Username:</label>
    <input type="text" id="username" name="username" required><br><br>

    <label for="password">Password:</label>
    <input type="password" id="password" name="password" required><br><br>

    <button type="submit">Login</button>
</form>

<jsp:include page="/common/footer.jsp" />

