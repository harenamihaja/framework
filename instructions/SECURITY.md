# Sécurité — Guide d'utilisation (annotations)

Ce document décrit le fonctionnement minimal de la sécurité implémentée dans le framework :
annotations, configuration et tests.

## 1) Principe

- On annote les méthodes d'action des contrôleurs avec :
  - `@Role("roleName")` : seul(s) l(es) utilisateur(s) du rôle spécifié peuvent appeler la méthode.
  - `@Authorized` : l'utilisateur doit être connecté (présence d'un attribut de session configuré).
  - `@Anonym` : accessible à tous (pas d'authentification requise).

- Les clés de session associées à ces annotations sont définies dans la configuration :
  - Soit dans `WEB-INF/security.xml`
  - Soit dans un bloc `<security>` ajouté à `WEB-INF/web.xml`

Exemple de configuration (déjà fournie) :

```xml
<security>
  <keys>
    <role>userRole</role>
    <authorized>user</authorized>
    <anonym>anon</anonym>
  </keys>
</security>
```

Cela signifie :
- la clé de session pour le rôle est `userRole` (ex : `session.setAttribute("userRole", "admin")`)
- la clé de session pour l'utilisateur authentifié est `user` (ex : `session.setAttribute("user", username)`)
- `anonym` est réservé mais n'est pas obligatoirement peuplé (utilisé uniquement pour lecture)

## 2) Comportement implémenté

- Au démarrage, `FrontServlet` charge les clés de sécurité :
  - Cherche d'abord `WEB-INF/security.xml`.
  - Si absent, il cherche un bloc `<security>` dans `WEB-INF/web.xml`.
- Avant d'invoquer une méthode contrôleur, `FrontServlet` appelle `checkAuthorization(...)` :
  - Si la méthode est annotée `@Anonym` → autorisé.
  - Si `@Authorized` → vérifie que la session existe et que `session.getAttribute(<authorizedKey>) != null`.
  - Si `@Role(...)` → vérifie que `session.getAttribute(<roleKey>)` existe et correspond à un des rôles déclarés.
  - En cas d'échec, le framework renvoie un `403` (ou `500` si la clé de configuration est manquante).

## 3) Comment annoter vos contrôleurs

Exemples :

```java
@Controller
public class MonController {

  @GetMapping("/public")
  @Anonym
  public ModelView publicPage() { ... }

  @GetMapping("/profile")
  @Authorized
  public ModelView profile(@Session Map<String,Object> session) { ... }

  @GetMapping("/admin")
  @Role({"admin"})
  public ModelView adminPage() { ... }
}
```

Remarques :
- L'attribut de session utilisé pour `@Authorized` est la valeur configurée par la clé `authorized`.
- L'attribut de session utilisé pour `@Role` est la valeur configurée par la clé `role`.

## 4) Pages et contrôleurs de test fournis

Le projet contient un `SecurityController` de test (routes) :
- `GET /test/anonym` → page accessible à tous
- `GET /test/auth` → requiert `@Authorized`
- `GET /test/role` → requiert `@Role("admin")`
- `GET /login` et `POST /login` → formulaire simple qui crée en session `user` et `userRole`
- `POST /logout` → invalide la session

Vues de test :
- `views/security/login.jsp`
- `views/security/test-anonym.jsp`
- `views/security/test-auth.jsp`
- `views/security/test-role.jsp`

## 5) Instructions de test (rapide)

1. Construire et déployer :

```cmd
deploy.bat
```

2. Dans Tomcat, s'assurer qu'il n'y a pas d'anciennes copies de l'application :

```powershell
& 'C:\apache-tomcat-10.1.28\bin\shutdown.bat'
Remove-Item -Recurse -Force 'C:\apache-tomcat-10.1.28\webapps\framework' -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force 'C:\apache-tomcat-10.1.28\work\Catalina\localhost\framework' -ErrorAction SilentlyContinue
& 'C:\apache-tomcat-10.1.28\bin\startup.bat'
```

3. Tester les pages :
- Ouvrir `http://localhost:8080/framework/test/anonym` → doit afficher la page anonyme.
- Ouvrir `http://localhost:8080/framework/test/auth` → doit renvoyer 403 si non connecté.
- Ouvrir `http://localhost:8080/framework/login` → soumettre un username et un rôle (`user` ou `admin`).
- Après login, réessayer `/test/auth` et `/test/role` (si rôle `admin`) pour vérifier l'accès.

## 6) Personnalisation et bonnes pratiques

- Vous pouvez modifier les noms de clés (par ex. `user`, `userRole`) dans `WEB-INF/security.xml` ou dans le `<security>` de `web.xml`.
- Par défaut, les accès refusés renvoient un `403` simple. Vous pouvez modifier `FrontServlet.checkAuthorization` pour rediriger vers une page de login ou afficher une vue personnalisée.
- Protégez la mise en session côté login (vérifier identifiants réels si nécessaire) — l'exemple fourni est volontairement minimal pour tests.

## 7) Problèmes fréquents

- `security.xml` absent et `<security>` absent dans `web.xml` → le framework signale l'absence et utilise le comportement par défaut (les méthodes annotées `@Authorized`/`@Role` échoueront si les clés manquent).
- Vérifiez que les noms d'attributs de session utilisés par votre code de login correspondent aux clés configurées.

---

Si tu veux, je peux :
- reconstruire et déployer automatiquement maintenant, ou
- remplacer les réponses 403 par une redirection vers `/login`.
