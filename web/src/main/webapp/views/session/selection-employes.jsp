<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Ma sélection</title>
</head>
<body>
<h1>Employés sélectionnés</h1>

<c:if test="${not empty message}">
    <p><strong>${message}</strong></p>
</c:if>

<c:if test="${not empty selection}">
    <ul>
        <c:forEach var="e" items="${selection}">
            <li>${e.nom} - ${e.age} ans (ID: ${e.id})</li>
        </c:forEach>
    </ul>
    <p><a href="${pageContext.request.contextPath}/employe/vider-selection">Vider la sélection</a></p>
</c:if>

<p><a href="${pageContext.request.contextPath}/employe/ajouter">+ Ajouter un autre</a></p>
<p><a href="${pageContext.request.contextPath}/employes">← Retour à la liste complète</a></p>
</body>
</html>