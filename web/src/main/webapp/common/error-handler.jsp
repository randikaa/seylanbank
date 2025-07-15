<%--
  Created by IntelliJ IDEA.
  User: Randika perera
  Date: 7/14/2025
  Time: 7:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<html>
<head>
    <title>Application Error</title>
</head>
<body>
<h2>An Unexpected Error Occurred</h2>
<p>We are sorry for the inconvenience. Please try again later.</p>
<%-- For debugging purposes only. Do not show this in production. --%>
<pre>
        Error: <%= exception.getMessage() %>
        Stack Trace:
        <% exception.printStackTrace(new java.io.PrintWriter(out)); %>
    </pre>
</body>
</html>
