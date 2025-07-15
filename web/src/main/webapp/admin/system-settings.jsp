<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('SUPER_ADMIN')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>System Settings (Super Admin Only)</h2>
<form>
    <fieldset>
        <legend>Interest Rates</legend>
        Savings Account Rate (%): <input type="number" name="savingsRate" step="0.01"><br>
        Checking Account Rate (%): <input type="number" name="checkingRate" step="0.01"><br>
    </fieldset>
    <br>
    <fieldset>
        <legend>System Status</legend>
        Enable Maintenance Mode: <input type="checkbox" name="maintenanceMode"><br>
    </fieldset>
    <br>
    <input type="submit" value="Update Settings">
</form>
<jsp:include page="../common/footer.jsp"/>