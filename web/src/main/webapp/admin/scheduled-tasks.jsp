<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:48 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.List, com.randika.seylanbank.core.model.ScheduledTask, javax.naming.InitialContext" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!pageContext.request.isUserInRole('ADMIN') && !pageContext.request.isUserInRole('SUPER_ADMIN')}">
    <c:redirect url="../unauthorized.jsp"/>
</c:if>
<jsp:include page="../common/header.jsp"/>
<jsp:include page="../common/navigation.jsp"/>
<h2>Pending Scheduled Tasks</h2>
<%
    try {
        InitialContext ctx = new InitialContext();
        com.randika.seylanbank.core.service.ScheduledOperationService scheduledTaskService = (com.randika.seylanbank.core.service.ScheduledOperationService) ctx.lookup("java:global/ear/banking/ScheduledOperationsBean!com.randika.seylanbank.core.service.ScheduledOperationService");
        request.setAttribute("tasks", scheduledTaskService.getPendingTasks());
    } catch (Exception e) {
        request.setAttribute("errorMessage", "Error loading scheduled tasks: " + e.getMessage());
    }
%>
<c:if test="${not empty errorMessage}"><p style="color:red;"><c:out value="${errorMessage}"/></p></c:if>
<table border="1">
    <thead><tr><th>ID</th><th>Type</th><th>From Account</th><th>To Account</th><th>Amount</th><th>Scheduled For</th><th>Action</th></tr></thead>
    <tbody>
    <c:forEach var="task" items="${tasks}">
        <tr>
            <td><c:out value="${task.id}"/></td>
            <td><c:out value="${task.taskType}"/></td>
            <td><c:out value="${task.fromAccountId}"/></td>
            <td><c:out value="${task.toAccountId}"/></td>
            <td><fmt:formatNumber value="${task.amount}" type="currency"/></td>
            <td><fmt:formatDate value="${task.scheduledDate}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td><a href="scheduled-tasks?action=cancel&taskId=${task.id}">Cancel</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<jsp:include page="../common/footer.jsp"/>
