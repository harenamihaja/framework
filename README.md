# Mini framework web Java (type Spring MVC)

Framework web Java construit from scratch pour comprendre les principes d'un MVC moderne (Front Controller, routing par annotations, binding de parametres, vues JSP, JSON, session, securite, upload). Le depot contient aussi une webapp de demonstration.

## Structure du depot

- monframework/ : le coeur du framework (FrontServlet, annotations, scanner, modeles)
- web/ : application de demo (controllers, vues JSP/HTML, config web.xml)
- instructions/ : documentation de sprint et plans de test
- deploy.bat : build + packaging WAR + deploy Tomcat

## Fonctionnalites du framework

- Front Controller unique (FrontServlet)
- Routing par annotations : @UrlMapping, @GetMapping, @PostMapping
- Path variables via @PathVariable
- Binding de parametres via @RequestParam
- ModelView pour transmettre les donnees aux JSP
- Reponses JSON via @JsonResponse
- Gestion session via @Session
- Upload fichiers via @FileUpload
- Securite declarative : @Anonym, @Authorized, @Role
- Gestion erreurs HTTP : 404, 405, 403

## Prerequis

- Java 17 (JDK)
- Tomcat 10.1.x
- Windows (scripts batch)

## Build et deploiement (Windows)

Le script principal compile le framework, compile la webapp, genere le WAR, puis le copie dans Tomcat.

1) Ouvrir et ajuster les chemins si besoin dans deploy.bat
- root
- tomcat_dir
- JAVAC et JAR (chemin JDK)

2) Executer le script :

```bat
.\deploy.bat
```

3) Demarrer Tomcat (si pas deja lance), puis ouvrir :

- http://localhost:8080/framework/index-sprint11.html
- http://localhost:8080/framework/session-test.html

## Tester rapidement la demo

- Formulaire session : http://localhost:8080/framework/session/form
- Panier session : http://localhost:8080/framework/session/afficher

## Configuration web

La config est dans :
- web/src/main/webapp/WEB-INF/web.xml
- web/src/main/webapp/WEB-INF/security.xml

## Notes utiles

- Le scan des controllers se base sur le package configure dans web.xml.
- Les endpoints et vues de demo sont dans web/src/main/java/controller et web/src/main/webapp/views.
- Les instructions de sprint et plan de test sont dans instructions/.

## Auteurs

Projet realise dans le cadre d'un sprint pedagogique (mini framework MVC Java).
