<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/14/2025
  Time: 7:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<nav>
    <c:if test="${not empty sessionScope.username}">
        <p>Welcome, <b><c:out value="${sessionScope.username}"/></b> (<c:out value="${sessionScope.userRole}"/>)</p>
    </c:if>
    <ul>
        <c:choose>
            <c:when test="${sessionScope.userRole == 'ADMIN' || sessionScope.userRole == 'SUPER_ADMIN'}">
                <li><a href="${pageContext.request.contextPath}/admin/dashboard.jsp">Admin Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/customers?action=list">Customer Management</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/accounts?action=list">Account Management</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/transaction-reports.jsp">Transaction Reports</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/scheduled-tasks.jsp">Scheduled Tasks</a></li>
                <c:if test="${sessionScope.userRole == 'SUPER_ADMIN'}">
                    <li><a href="${pageContext.request.contextPath}/admin/system-settings.jsp">System Settings</a></li>
                </c:if>
                <li><a href="${pageContext.request.contextPath}/logout.jsp">Logout</a></li>
            </c:when>

            <c:when test="${sessionScope.userRole == 'CUSTOMER'}">
                <li><a href="${pageContext.request.contextPath}/customer/dashboard.jsp">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/customer/account-summary.jsp">Account Summary</a></li>
                <li><a href="${pageContext.request.contextPath}/customer/fund-transfer.jsp">Fund Transfer</a></li>
                <li><a href="${pageContext.request.contextPath}/customer/transaction-history.jsp">Transaction History</a></li>
                <li><a href="${pageContext.request.contextPath}/customer/profile.jsp">My Profile</a></li>
                <li><a href="${pageContext.request.contextPath}/logout.jsp">Logout</a></li>
            </c:when>

            <c:otherwise>
                <li><a href="${pageContext.request.contextPath}/index.jsp">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/login.jsp">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register.jsp">Register</a></li>
            </c:otherwise>
        </c:choose>
    </ul>
    <hr>
</nav>

