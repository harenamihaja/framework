<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
</head>
<body>
    <h1>Login (test)</h1>
    <form action="login" method="post">
        <label>Username: <input type="text" name="username"/></label><br/>
        <label>Role: <select name="role">
            <option value="user">user</option>
            <option value="admin">admin</option>
        </select></label><br/>
        <button type="submit">Login</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/test/anonym">Test anonym</a></p>
    <p><a href="${pageContext.request.contextPath}/test/auth">Test authorized</a></p>
    <p><a href="${pageContext.request.contextPath}/test/role">Test role=admin</a></p>
</body>
</html>
