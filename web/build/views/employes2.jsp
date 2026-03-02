<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${titre}</title>
</head>
<body>
<h1>Liste des employés</h1>

<c:if test="${not empty employes}">
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Âge</th>
            <th>Action</th>
        </tr>
        <c:forEach var="e" items="${employes}">
            <tr>
                <td>${e.id}</td>
                <td>${e.nom}</td>
                <td>${e.age}</td>
                <td><a href="${pageContext.request.contextPath}/employe/detail?id=${e.id}">Détail</a></td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<p><a href="${pageContext.request.contextPath}/employe/ajouter">+ Ajouter un employé</a></p>
<p><a href="${pageContext.request.contextPath}/employe/selectionnes">Voir ma sélection</a></p>
</body>
</html>