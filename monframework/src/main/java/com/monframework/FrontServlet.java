package com.monframework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import com.monframework.scanner.Route;
import com.monframework.models.ModelView;
import com.monframework.scanner.ControllerScanner;
import com.monframework.annotations.PathVariable;  // Import the new annotation
import com.monframework.annotations.RequestParam;
import com.monframework.exceptions.MethodNotAllowedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class FrontServlet extends HttpServlet {

    private Map<String, List<Route>> routeMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();

        // Scanner le package des controllers
        String controllerPackage = getInitParameter("controllerPackage");
        if (controllerPackage == null) {
            controllerPackage = "controller"; // Package par défaut
        }

        System.out.println("=== Scanning package: " + controllerPackage + " ===");

        // Récupérer toutes les routes
        List<Route> routes = ControllerScanner.getRoutes(controllerPackage);

        // Stocker les routes dans une Map<URL, List<Route>> pour GET/POST
        for (Route route : routes) {
            String url = route.getUrl();
            // Si la clé n'existe pas, créer une nouvelle liste
            routeMap.computeIfAbsent(url, k -> new ArrayList<>()).add(route);

            System.out.println("Route enregistrée: " + url + " (" + route.getHttpMethod() + ")"
                    + " -> " + route.getController().getSimpleName()
                    + "." + route.getMethod().getName() + "()");
        }

        System.out.println("=== " + routes.size() + " route(s) trouvée(s) ===");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

// Méthode commune
    private void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            chercherRessource(req, resp);
        } catch (MethodNotAllowedException e) {
            resp.setStatus(e.getStatusCode());
            resp.setHeader("Allow", String.join(", ", e.getAllowedMethods()));
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>405 - Method Not Allowed</h1>");
            out.println("<p>La méthode <b>" + req.getMethod() + "</b> n'est pas autorisée ici.</p>");
            out.println("<p>Méthodes autorisées : <b>" + String.join(", ", e.getAllowedMethods()) + "</b></p>");
            out.println("<p><a href='" + req.getContextPath() + "/'>Retour</a></p>");
        }
    }

    private void chercherRessource(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());
        String httpMethod = req.getMethod();
        System.out.println("Recherche de la ressource pour l'URL : " + path + " [" + httpMethod + "]");
        Route matchedRoute = null;
        Map<String, String> routeParams = null;

        // 1. Recherche exacte (routes statiques)
        if (routeMap.containsKey(path)) {
            List<Route> candidates = routeMap.get(path);
            matchedRoute = findByHttpMethod(candidates, httpMethod);
            if (matchedRoute == null) {
                // URL existe mais méthode non supportée → 405
                Set<String> allowed = candidates.stream()
                        .map(Route::getHttpMethod)
                        .collect(java.util.stream.Collectors.toSet());
                throw new MethodNotAllowedException(allowed);
            }
        }

        // 2. Recherche dynamique
        if (matchedRoute == null) {
            for (List<Route> candidates : routeMap.values()) {
                for (Route route : candidates) {
                    Matcher matcher = route.getPattern().matcher(path);
                    if (matcher.matches()) {
                        if (route.matchesHttpMethod(httpMethod)) {
                            matchedRoute = route;
                            routeParams = new HashMap<>();
                            List<String> paramNames = route.getParamNames();
                            for (int i = 0; i < paramNames.size(); i++) {
                                routeParams.put(paramNames.get(i), matcher.group(i + 1));
                            }
                            break;
                        } else {
                            // Pattern matche mais méthode HTTP non supportée → 405
                            Set<String> allowed = candidates.stream()
                                    .filter(r -> r.getPattern().matcher(path).matches())
                                    .map(Route::getHttpMethod)
                                    .collect(java.util.stream.Collectors.toSet());
                            throw new MethodNotAllowedException(allowed);
                        }
                    }
                }
                if (matchedRoute != null) {
                    break;
                }
            }
        }

        // 3. Si route trouvée → exécuter
        if (matchedRoute != null) {
            if (routeParams != null) {
                req.setAttribute("routeParams", routeParams);
            }
            executerRoute(matchedRoute, req, resp);
            return;
        }

        // 4. Page d'accueil
        if ("/".equals(path)) {
            showRouteList(req, resp);
            return;
        }

        // 5. Ressources statiques
        if (getServletContext().getResource(path) != null) {
            getServletContext().getNamedDispatcher("default").forward(req, resp);
            return;
        }

        // 6. 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().println("<h1>404 - Not Found</h1><p>URL introuvable : " + path + "</p>");
    }

    private void executerRoute(Route route, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Object controller = route.getController().getDeclaredConstructor().newInstance();

            //ilay fonction mifanaraka amle url 
            Method method = route.getMethod();

            // 1. Paramètres du formulaire (POST + query string GET)
            Map<String, String[]> parameterMap = req.getParameterMap();

            // 2. Paramètres d'URL dynamique : /dept/{id} → id=5
            //le map avy any ambony (nom de param,valeur)
            @SuppressWarnings("unchecked")
            Map<String, String> routeParams = (Map<String, String>) req.getAttribute("routeParams");

            // 3. Noms des placeholders dans l'ordre (ex: ["id", "name"])
            //getter avy any am classe Route
            List<String> urlParamNames = route.getParamNames();

            Object result;

            if (method.getParameterCount() == 0) {
                result = method.invoke(controller);
            } else {
                Parameter[] parameters = method.getParameters();

                // === NOUVELLE FONCTIONNALITÉ : Détecter si on a un paramètre Map<String, Object> ===
                boolean hasMapParameter = false;
                int mapParamIndex = -1;
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType().equals(Map.class)
                            && parameters[i].getParameterizedType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.Object>")) {
                        if (hasMapParameter) {
                            throw new ServletException("Plusieurs paramètres de type Map<String, Object> détectés. Un seul est autorisé.");
                        }
                        hasMapParameter = true;
                        mapParamIndex = i;
                    }
                }

                Object[] args = new Object[parameters.length];

                if (hasMapParameter) {
                    // === Cas 1 : Il y a un paramètre Map<String, Object> ===
                    // On permet qu'il y ait d'autres paramètres, mais on les ignore (ou on peut lever une exception si tu veux être strict)
                    // Ici on autorise uniquement la Map seule pour simplifier, mais tu peux ajuster
                    if (parameters.length > 1) {
                        // Option douce : on accepte mais on ignore les autres
                        // Option stricte : throw new ServletException("Lorsque Map<String,Object> est utilisé, il doit être le seul paramètre.");
                    }

                    // Construction de la Map avec tous les paramètres de la requête
                    Map<String, Object> requestData = new HashMap<>();
                    //  Map<String, String[]> parameterMap = req.getParameterMap();

                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        if (values.length == 1) {
                            requestData.put(key, values[0]);
                        } else {
                            requestData.put(key, values); // tableau si plusieurs valeurs
                        }
                    }

                    // Ajouter aussi les paramètres de route (PathVariable) si présents
                    if (routeParams != null) {
                        requestData.putAll(routeParams);
                    }

                    args[mapParamIndex] = requestData;

                    // Remplir les autres args avec null si besoin (cas rare)
                    for (int i = 0; i < args.length; i++) {
                        if (i != mapParamIndex) {
                            args[i] = null;
                        }
                    }

                    result = method.invoke(controller, args);

                } else {
                    // === Cas 2 : Comportement EXISTANT (inchangé) ===
                    // Ton code actuel pour gérer @RequestParam, PathVariable, etc.
                    Object[] argsClassic = new Object[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        Parameter param = parameters[i];
                        Class<?> type = param.getType();
                        String paramName = param.getName();

                        String fieldName;
                        boolean required = true;

                        RequestParam rp = param.getAnnotation(RequestParam.class);
                        if (rp != null) {
                            fieldName = rp.value().isEmpty() ? paramName : rp.value();
                            required = rp.required();
                        } else {
                            fieldName = paramName;
                        }

                        String valueStr = null;

                        if (parameterMap.containsKey(fieldName)) {
                            valueStr = parameterMap.get(fieldName)[0];
                        } else if (routeParams != null) {
                            String lookup = paramName.startsWith("arg") ? urlParamNames.get(i) : fieldName;
                            valueStr = routeParams.get(lookup);
                        }

                        if (required && (valueStr == null || valueStr.isEmpty())) {
                            throw new ServletException("Paramètre requis manquant : " + fieldName);
                        }

                        if (valueStr == null) {
                            if (type.isPrimitive()) {
                                throw new ServletException("Paramètre primitif non initialisé : " + fieldName);
                            }
                            argsClassic[i] = null;
                            req.setAttribute(fieldName, null);
                            continue;
                        }

                        Object value = switch (type.getSimpleName()) {
                            case "int", "Integer" ->
                                Integer.parseInt(valueStr);
                            case "long", "Long" ->
                                Long.parseLong(valueStr);
                            case "double", "Double" ->
                                Double.parseDouble(valueStr);
                            case "boolean", "Boolean" ->
                                Boolean.parseBoolean(valueStr);
                            default ->
                                valueStr;
                        };

                        argsClassic[i] = value;
                        req.setAttribute(fieldName, value);
                    }
                    result = method.invoke(controller, argsClassic);
                }
            }

            // === Gestion du retour (ModelView ou String) ===
            if (result instanceof ModelView mv) {
                String view = mv.getView();
                if (view == null || view.isEmpty()) {
                    throw new ServletException("ModelView sans vue définie");
                }

                // Ajoute les données du controller
                mv.getMapData().forEach((k, v) -> req.setAttribute(k, v));

                req.getRequestDispatcher(view).forward(req, resp);
            } else if (result instanceof String str) {
                resp.setContentType("text/html; charset=UTF-8");
                resp.getWriter().println(str);
            } else if (result == null) {
                throw new ServletException("Méthode retourne void. Attendu : ModelView ou String");
            } else {
                throw new ServletException("Type de retour non supporté : " + result.getClass());
            }

        } catch (Exception e) {
            resp.setStatus(500);
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>Erreur Framework</h1>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
    }

    private void showRouteList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<h1>Framework Spring-like</h1>");
        out.println("<h2>Routes disponibles :</h2>");
        out.println("<ul style='font-family: monospace; line-height: 1.8;'>");

        // Tri des URLs pour un affichage propre
        routeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String url = entry.getKey();
                    List<Route> routes = entry.getValue();

                    routes.forEach(r -> {
                        String methodColor = switch (r.getHttpMethod()) {
                            case "GET" ->
                                "#28a745";
                            case "POST" ->
                                "#dc3545";
                            case "ANY" ->
                                "#6f42c1";
                            default ->
                                "#000000";
                        };

                        out.printf(
                                "<li>"
                                + "  <a href='%s%s' style='text-decoration:none; font-weight:bold;'>%s</a> "
                                + "  <span style='color:%s; font-weight:bold; padding:2px 6px; border-radius:4px;'>%s</span> → "
                                + "  <code>%s.%s()</code>"
                                + "</li>%n",
                                req.getContextPath(), url, url,
                                methodColor, r.getHttpMethod(),
                                r.getController().getSimpleName(),
                                r.getMethod().getName()
                        );
                    });
                });

        out.println("</ul>");
        out.println("<p><small>Framework développé avec amour à Madagascar</small></p>");
    }

    private Route findByHttpMethod(List<Route> routes, String httpMethod) {
        for (Route route : routes) {
            if (route.matchesHttpMethod(httpMethod)) {
                return route;
            }
        }
        return null;
    }
}
