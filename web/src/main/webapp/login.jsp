<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/13/2025
  Time: 11:46 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="common/header.jsp"/>
<jsp:include page="common/navigation.jsp"/>
<h2>Login</h2>
<c:if test="${not empty param.error}">
    <p style="color:red;">Invalid username or password. Please try again.</p>
</c:if>
<form action="j_security_check" method="post">
    Username: <input type="text" name="j_username" required><br>
    Password: <input type="password" name="j_password" required><br>
    <input type="submit" value="Login">
</form>
<jsp:include page="common/footer.jsp"/>
