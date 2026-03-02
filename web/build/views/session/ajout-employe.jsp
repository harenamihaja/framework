<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${titre}</title>
</head>
<body>
<h1>Ajouter un employé (en session)</h1>

<form method="POST" action="${pageContext.request.contextPath}/employe/ajouter">
    <label>Nom : <input type="text" name="nom" required></label><br><br>
    <label>Âge : <input type="number" name="age" min="18" max="70" required></label><br><br>
    <button type="submit">Ajouter à ma sélection</button>
</form>

<p><a href="ajouter">← Retour à la liste</a></p>
</body>
</html>