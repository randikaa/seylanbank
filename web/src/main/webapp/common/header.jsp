<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/14/2025
  Time: 7:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>SeylanBank - ${pageTitle}</title>
    <meta charset="UTF-8">
</head>
<body>
<h1>SeylanBank Core Banking System</h1>
<c:if test="${not empty sessionScope.username}">
<p>Welcome, ${sessionScope.username} (${sessionScope.userRole})</p>
</c:if>
