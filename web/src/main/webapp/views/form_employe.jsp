<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.Employe" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<h1>Formulaire de Paiement Employé</h1>

<form action="save-paiement" method="get">
    <input type="text" name="paiement.montant" placeholder="Montant" required />
    <select name="paiement.employe.id">
        <option value="">Sélectionner un employé</option>
        <% 
            // Simuler une liste d'employés pour l'exemple
            List<model.Employe> employes = new ArrayList<>();
            model.Employe emp1 = new model.Employe();
            emp1.setId(1);
            emp1.setNom("Dupont");
            emp1.setAge(30);
            employes.add(emp1);
            model.Employe emp2 = new model.Employe();
            emp2.setId(2);
            emp2.setNom("Martin");
            emp2.setAge(45);
            employes.add(emp2);
            
            for(model.Employe employe : employes) {
        %>
            <option value="<%= employe.getId() %>"><%= employe.getNom() %> (Âge: <%= employe.getAge() %>)</option>
        <% } %>
    </select>
    <button type="submit">Sauvegarder</button>
</form>