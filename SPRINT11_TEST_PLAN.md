# 🧪 Sprint 11 - Guide de Test Complet

## Version: Sprint 11 Session Management

---

## ✅ Checklist de vérification avant le test

### Code backend
- [ ] `FrontServlet.java` - Gère `@Session` et les redirects
- [ ] `SessionController.java` - 4 endpoints pour tester
- [ ] Annotations importées correctement
- [ ] Logique de copie session bidirectionnelle présente

### Code frontend
- [ ] `form-produit.jsp` - Formulaire de création
- [ ] `panier.jsp` - Affichage du panier
- [ ] `session-test.html` - Page de démarrage
- [ ] `index-sprint11.html` - Menu principal

### Configuration
- [ ] `web.xml` - FrontServlet configuré avec package "controller"
- [ ] ControllerScanner - Scanne le package correctement
- [ ] Tomcat - Prêt à déployer

---

## 🚀 Scénario de test complet

### 📋 Test 1: Vérifier la détection des routes

**Objective:** Vérifier que FrontServlet détecte correctement les 4 routes

**Étapes:**
1. Démarrer Tomcat
2. Vérifier la console pour ces logs:
```
Route enregistrée: /session/form (ANY)
Route enregistrée: /session/ajouter (ANY)
Route enregistrée: /session/afficher (ANY)
Route enregistrée: /session/vider (ANY)
```

**Résultat attendu:** ✅ 4 routes détectées

---

### 📝 Test 2: Accéder au formulaire

**Objective:** Vérifier que le formulaire d'ajout se charge correctement

**Étapes:**
1. Ouvrir: `http://localhost:8080/web/session-test.html`
2. Cliquer sur "Voir la démo"
3. Cliquer sur "Commencer le test"

**Résultat attendu:** ✅ Page `form-produit.jsp` affichée avec:
- Champ "Nom du produit"
- Champ "Prix"
- Boutons "Ajouter au panier" et "Voir panier"

---

### 🛒 Test 3: Ajouter un premier produit

**Objective:** Tester la copie HttpSession → Map → HttpSession

**Étapes:**
1. Remplir le formulaire:
   - Nom: `Livre Java Avancé`
   - Prix: `59.99`
2. Cliquer "Ajouter au panier"
3. Vérifier la console pour:
```
=== Ajout en session ===
Session avant: {}
Session après: {panier=[Livre Java Avancé - 59.99€], nombreArticles=1, dernier=Livre Java Avancé}
=== Panier mis à jour ===
```

**Résultat attendu:** ✅ Redirection vers `/session/afficher` avec:
- 1 article affiché
- Produit visible dans la liste
- Message "Panier : 1 article(s)"

---

### ➕ Test 4: Ajouter un deuxième produit

**Objective:** Tester la persistance et l'accumulation en session

**Étapes:**
1. Cliquer "Ajouter un produit"
2. Remplir:
   - Nom: `Souris Logitech`
   - Prix: `29.99`
3. Cliquer "Ajouter au panier"
4. Vérifier la console:
```
Session avant: {panier=[...], nombreArticles=1, ...}
Session après: {panier=[...2 items...], nombreArticles=2, dernier=Souris Logitech}
```

**Résultat attendu:** ✅ Panier affiche:
- 2 articles
- Les 2 produits listés
- Dernier ajout: "Souris Logitech"

---

### 🔄 Test 5: Actualiser la page (test de persistance)

**Objective:** Vérifier que la session persiste après actualisation

**Étapes:**
1. Appuyer sur F5 (ou Ctrl+R)
2. Vérifier que:
   - Les 2 produits sont toujours affichés
   - Le compteur reste à 2
   - Aucune donnée n'a été perdue

**Résultat attendu:** ✅ Les données persistent!
```
Session: {panier=[Livre Java Avancé - 59.99€, Souris Logitech - 29.99€], 
          nombreArticles=2, 
          dernier=Souris Logitech}
```

---

### ➕ Test 6: Ajouter un produit supplémentaire

**Objective:** Tester que plusieurs ajouts fonctionnent

**Étapes:**
1. Cliquer "Ajouter un produit"
2. Ajouter: `Clavier Mécanique` - `149.99`
3. Vérifier:
   - 3 articles dans le panier
   - Tous les produits listés

**Résultat attendu:** ✅ Panier = 3 articles:
1. Livre Java Avancé - 59.99€
2. Souris Logitech - 29.99€
3. Clavier Mécanique - 149.99€

---

### 🗑️ Test 7: Vider le panier

**Objective:** Tester la suppression des données de session

**Étapes:**
1. Cliquer "Vider le panier"
2. Vérifier la console:
```
=== Panier vidé ===
```
3. Vérifier que:
   - Message "Le panier est vide"
   - Aucun produit affiché
   - Bouton "Vider le panier" disparu

**Résultat attendu:** ✅ Session vide:
```
Session: {panier=null, nombreArticles=null, dernier=null}
// OU
Session: {} (vide)
```

---

### ✅ Test 8: Re-actualiser après vidage

**Objective:** Confirmer que le vidage est persisté

**Étapes:**
1. Appuyer F5
2. Vérifier que le panier reste vide

**Résultat attendu:** ✅ Panier reste vide

---

### 🔁 Test 9: Nouveau cycle - Ajouter un produit différent

**Objective:** Tester un nouveau cycle de session

**Étapes:**
1. Cliquer "Ajouter un produit"
2. Ajouter: `MacBook Pro` - `1999.99`
3. Vérifier le panier

**Résultat attendu:** ✅ Le panier contient seul le nouveau produit:
- 1 article
- `MacBook Pro - 1999.99€`

---

### 🌐 Test 10: Vérifier les logs complets

**Objective:** Valider que tout fonctionne côté serveur

**Logs attendus dans la console:**

```
=== Scanning package: controller ===
Route enregistrée: /session/form (ANY) -> SessionController.afficherForm()
Route enregistrée: /session/ajouter (ANY) -> SessionController.ajouterProduit()
Route enregistrée: /session/afficher (ANY) -> SessionController.afficherPanier()
Route enregistrée: /session/vider (ANY) -> SessionController.viderPanier()
=== 4 route(s) trouvée(s) ===

[Lors d'un ajout]
=== Ajout en session ===
Session avant: {...}
Session après: {...}
=== Panier mis à jour ===

[Lors de l'affichage]
=== Affichage panier ===
Session: {...}

[Lors du vidage]
=== Panier vidé ===
```

**Résultat attendu:** ✅ Tous les logs présents

---

## 📊 Tableau récapitulatif des tests

| # | Test | Étapes | Résultat | ✅/❌ |
|---|------|--------|----------|------|
| 1 | Routes détectées | Vérifier console | 4 routes | |
| 2 | Accès formulaire | /session/form | Form affiché | |
| 3 | Ajouter produit | Form + Submit | 1 article | |
| 4 | Ajouter produit | Form + Submit | 2 articles | |
| 5 | Actualiser | F5 | 2 articles persiste | |
| 6 | Ajouter produit | Form + Submit | 3 articles | |
| 7 | Vider panier | Clic vider | Panier vide | |
| 8 | Actualiser | F5 | Reste vide | |
| 9 | Nouveau cycle | Form + Submit | 1 article | |
| 10 | Logs complets | Console | Tous présents | |

---

## 🎯 Critères de succès

✅ **Tous les tests passent** → Sprint 11 complète!

### Vérifications finales

- [ ] Les 4 routes sont détectées et fonctionnelles
- [ ] La copie HttpSession → Map fonctionne
- [ ] La copie Map → HttpSession persiste les données
- [ ] Les redirects fonctionnent (`redirect:/url`)
- [ ] La session persiste après actualisation
- [ ] Les données peuvent être vidées
- [ ] Aucune erreur en console

---

## 🐛 Troubleshooting

### Problem: Routes non détectées
**Solution:** Vérifier que `SessionController` a bien l'annotation `@Controller`

### Problem: Panier vide après ajout
**Solution:** Vérifier que FrontServlet copie bien la session vers le Map (ligne avec `@Session`)

### Problem: Données perdues après actualisation
**Solution:** Vérifier que la session est copiée VERS HttpSession après invocation (dernière partie de `executerRoute`)

### Problem: Gestion erreur 404
**Solution:** Vérifier les mappings d'URL `/session/form`, `/session/ajouter`, etc.

---

## 📞 Support

Si vous rencontrez des problèmes:
1. Vérifier la console Tomcat pour les errors
2. Consulter `SPRINT11_SYNTHESE.md`
3. Vérifier que FrontServlet importe `@Session`
4. Vérifier les noms des attributs dans JSP

---

## ✨ Conclusion

Après ces 10 tests, vous aurez validé:
- ✅ l'annotation `@Session` fonctionne
- ✅ La copie bidirectionnelle de session fonctionne
- ✅ Les redirects fonctionnent
- ✅ Votre framework est prêt pour la production!

**Bonne chance!** 🎉

