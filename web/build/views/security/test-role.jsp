<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Role</title></head>
<body>
<h1>Role-protected page</h1>
<p>User: ${user}</p>
<p>Role: ${role}</p>
<form action="logout" method="post"><button>Logout</button></form>
<p><a href="/">Home</a></p>
</body>
</html>
