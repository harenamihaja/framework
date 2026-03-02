# Sprint 11 - Session Management avec @Session

## 📋 Résumé de l'implémentation

Vous avez implémenté la fonctionnalité de gestion des sessions HTTP avec l'annotation `@Session` dans votre framework Spring-like.

### ✅ Fichiers modifiés/créés

#### 1. **FrontServlet.java** (Modifié)
- ✅ Importation de l'annotation `@Session`
- ✅ Détection des paramètres annotés avec `@Session`
- ✅ Copie de `HttpSession` vers `Map<String, Object>` avant l'invocation
- ✅ Copie de `Map<String, Object>` vers `HttpSession` après l'invocation (persiste les changements)
- ✅ Gestion des redirects avec le pattern `"redirect:/url"`

#### 2. **SessionController.java** (Créé/Réécrit)
Simplifié pour tester uniquement la fonctionnalité de session avec un exemple de **panier de produits**:

```java
@Controller
public class SessionController {
    
    @UrlMapping(url = "/session/form")
    public ModelView afficherForm()
    // Affiche le formulaire d'ajout de produit
    
    @UrlMapping(url = "/session/ajouter")
    public ModelView ajouterProduit(
        @RequestParam("nom") String nom,
        @RequestParam("prix") Double prix,
        @Session Map<String, Object> session)
    // Ajoute un produit au panier (stocké en session)
    
    @UrlMapping(url = "/session/afficher")
    public ModelView afficherPanier(@Session Map<String, Object> session)
    // Affiche le panier avec les produits en session
    
    @UrlMapping(url = "/session/vider")
    public ModelView viderPanier(@Session Map<String, Object> session)
    // Vide complètement le panier
}
```

#### 3. **Pages JSP créées**

- **form-produit.jsp** - Formulaire pour ajouter un produit
  - Champ nom (texte)
  - Champ prix (nombre)
  - Bouton "Ajouter au panier"
  - Lien vers "Voir panier"

- **panier.jsp** - Affichage du panier
  - Liste des produits en session
  - Compteur d'articles
  - Dernier produit ajouté
  - Boutons d'action (ajouter, vider)

- **session-test.html** - Page d'accueil
  - Explication de la fonctionnalité
  - Exemple de code
  - Lien de démarrage du test

---

## 🚀 Comment tester

### 1. **Déployer l'application**
```bash
cd e:\ITU\s5\MrNaina\sprint\framework\web
mvn clean package
# Déployer le WAR sur Tomcat
```

### 2. **Accéder à la page de test**
```
http://localhost:8080/web/session-test.html
```

### 3. **Tester les fonctionnalités**

**Étape 1 - Ajouter un produit:**
- Cliquez sur "Commencer le test"
- Remplissez le formulaire:
  - Nom: "Livre Java"
  - Prix: "49.99"
- Cliquez "Ajouter au panier"

**Étape 2 - Vérifier la session:**
- Vous êtes redirigé vers le panier
- Vous voyez "1 article(s)"
- Le produit "Livre Java - 49.99€" apparaît

**Étape 3 - Ajouter d'autres produits:**
- Cliquez "Ajouter un produit"
- Ajoutez: "Souris Logitech" - "29.99"
- Vérifiez que les 2 produits sont en session

**Étape 4 - Actualiser la page (F5):**
- Les produits restent! (la session persiste)

**Étape 5 - Vider le panier:**
- Cliquez "Vider le panier"
- Le panier revient vide

---

## 🔍 Fonctionnement technique

### Flux de la requête

```
[Utilisateur soumet le formulaire]
              ↓
[FrontServlet reçoit la requête]
              ↓
[Récupère HttpSession et copie dans Map]
              ↓
[Invoque SessionController.ajouterProduit()]
    ↳ Reçoit le Map<String, Object>
    ↳ Modifie le contenu du Map
              ↓
[Copie le Map modifié vers HttpSession]
              ↓
[Exécute la redirection]
              ↓
[L'utilisateur reçoit la réponse avec les données de session persistées]
```

### Annotations utilisées

```java
@Session Map<String, Object> session
```

- **Type d'argument**: OBLIGATOIREMENT `Map<String, Object>`
- **Comportement**: 
  - Avant: `HttpSession` → `Map` (copie)
  - Après: `Map` → `HttpSession` (persiste)

---

## 📝 Logs attendus dans la console

Au démarrage:
```
=== Scanning package: controller ===
Route enregistrée: /session/form (ANY) -> SessionController.afficherForm()
Route enregistrée: /session/ajouter (ANY) -> SessionController.ajouterProduit()
Route enregistrée: /session/afficher (ANY) -> SessionController.afficherPanier()
Route enregistrée: /session/vider (ANY) -> SessionController.viderPanier()
=== 4 route(s) trouvée(s) ===
```

Lors d'un ajout:
```
=== Ajout en session ===
Session avant: {}
Session après: {panier=[Livre Java - 49.99€], nombreArticles=1, dernier=Livre Java}
=== Panier mis à jour ===
```

---

## ✨ Points clés

✅ **Session persistante** - Les données restent même après actualisation  
✅ **Type-safe** - L'utilisation de `Map<String, Object>` est vérifiée  
✅ **Gestion de redirects** - Pattern `"redirect:/url"` supporté  
✅ **Méthode élégante** - Pas besoin d'accéder directement à `HttpSession`  

---

## 🎯 Prochaines étapes possibles

- Ajouter d'autres annotations (@GetMapping, @PostMapping)
- Implémenter `@JsonResponse` pour retourner du JSON
- Ajouter validation des paramètres
- Gérer les fichiers avec `@FileUpload`

