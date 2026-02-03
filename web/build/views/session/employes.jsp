<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Employe" %>

<html>
<head>
    <title><%= request.getAttribute("titre") %></title>
</head>
<body>

<h1>Liste des employés</h1>

<%
    List<Employe> employes = (List<Employe>) request.getAttribute("employes");

    if (employes != null && !employes.isEmpty()) {
%>

<table border="1">
    <tr>
        <th>ID</th>
        <th>Nom</th>
        <th>Âge</th>
        <th>Action</th>
    </tr>

    <%
        for (Employe e : employes) {
    %>
    <tr>
        <td><%= e.getId() %></td>
        <td><%= e.getNom() %></td>
        <td><%= e.getAge() %></td>
        <td>
            <a href="detail?id=<%= e.getId() %>">
                Détail
            </a>
        </td>
    </tr>
    <%
        }
    %>
</table>

<%
    } else {
%>
    <p>Aucun employé trouvé.</p>
<%
    }
%>

<p>
    <a href="<%= request.getContextPath() %>/employe/ajouter">+ Ajouter un employé</a>
</p>

</body>
</html>
