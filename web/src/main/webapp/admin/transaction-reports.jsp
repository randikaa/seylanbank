<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('ADMIN') && !pageContext.request.isUserInRole('SUPER_ADMIN')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Transaction Reports</h2>
<form action="reports" method="get">
    Report Type:
    <select name="reportType">
        <option value="dailyBalance">Daily Balance Report</option>
        <option value="customerList">All Customers Report</option>
        <%-- Add more report types here --%>
    </select><br>
    <input type="submit" value="Generate and Download Report">
</form>
<jsp:include page="../common/footer.jsp"/>
