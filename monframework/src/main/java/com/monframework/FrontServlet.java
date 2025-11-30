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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class FrontServlet extends HttpServlet {

    private Map<String, Route> routeMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();

        // Scanner le package des controllers (à adapter selon votre structure)
        String controllerPackage = getInitParameter("controllerPackage");
        if (controllerPackage == null) {
            controllerPackage = "controller"; // Package par défaut
        }

        System.out.println("=== Scanning package: " + controllerPackage + " ===");

        // Récupérer toutes les routes
        List<Route> routes = ControllerScanner.getRoutes(controllerPackage);

        // Stocker les routes dans une Map pour un accès rapide
        for (Route route : routes) {
            routeMap.put(route.getUrl(), route);
            System.out.println("Route enregistrée: " + route.getUrl()
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
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        chercherRessource(req, resp);
    }

    private void chercherRessource(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Vérifier si une route correspond à ce path (pour routes statiques)
        if (routeMap.containsKey(path)) {
            Route route = routeMap.get(path);
            executerRoute(route, req, resp);
            return;
        }

        // Essayer de matcher les routes dynamiques
        for (Route route : routeMap.values()) {
            Matcher matcher = route.getPattern().matcher(path);
            System.out.println("path " + path);
            if (matcher.matches()) {
                // Extraire les valeurs des paramètres dans une Map (nommés, e.g., "id" -> "123")
                Map<String, String> paramMap = new HashMap<>();
                List<String> paramNames = route.getParamNames();
                for (int i = 0; i < paramNames.size(); i++) {
                    paramMap.put(paramNames.get(i), matcher.group(i + 1));
                    System.out.println("paramNom " + paramNames.get(i) + " matcher i+1 " + matcher.group(i + 1));
                }

                req.setAttribute("routeParams", paramMap);

                executerRoute(route, req, resp);
                return;
            }
        }

        // Si c'est la racine
        if ("/".equals(path)) {
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().println("<h1>Framework Spring-like</h1>");
            resp.getWriter().println("<h2>Routes disponibles:</h2>");
            resp.getWriter().println("<ul>");
            for (String url : routeMap.keySet()) {
                Route r = routeMap.get(url);
                resp.getWriter().println("<li><a href='" + url + "'>" + url + "</a> -> "
                        + r.getController().getSimpleName() + "."
                        + r.getMethod().getName() + "()</li>");
            }
            resp.getWriter().println("</ul>");
            return;
        }

        // Vérifier si c'est une ressource statique (JSP, HTML, CSS, JS, etc.)
        boolean resourceExists = getServletContext().getResource(path) != null;
        if (resourceExists) {
            RequestDispatcher defaultDispatcher = getServletContext().getNamedDispatcher("default");
            defaultDispatcher.forward(req, resp);
        } else {
            // 404 - Page non trouvée
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>404 - Page non trouvée</h1>");
            out.println("<p>L'URL <b>" + path + "</b> ne correspond à aucune route.</p>");
            out.println("<p><a href='" + req.getContextPath() + "/'>Retour à l'accueil</a></p>");
        }
    }

    private void executerRoute(Route route, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Object controller = route.getController().getDeclaredConstructor().newInstance();
            Method method = route.getMethod();

            // 1. Paramètres du formulaire (POST + query string GET)
            Map<String, String[]> parameterMap = req.getParameterMap();

            // 2. Paramètres d'URL dynamique : /dept/{id} → id=5
            @SuppressWarnings("unchecked")
            Map<String, String> routeParams = (Map<String, String>) req.getAttribute("routeParams");

            // 3. Noms des placeholders dans l'ordre (ex: ["id", "name"])
            List<String> urlParamNames = route.getParamNames();

            Object result;

            if (method.getParameterCount() == 0) {
                result = method.invoke(controller);
            } else {
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    Class<?> type = param.getType();
                    String paramName = param.getName(); // nom du paramètre Java (ex: "nom")

                    String fieldName;        //  le nom qu’on va chercher dans le formulaire ou l’URL
                    boolean required = true;

                    RequestParam rp = param.getAnnotation(RequestParam.class);
                    if (rp != null) {
                        // Cas 1 : @RequestParam présent
                        fieldName = rp.value().isEmpty() ? paramName : rp.value();
                        required = rp.required();
                    } else {
                        // Cas 2 : pas d’annotation → comportement actuel (magique)
                        fieldName = paramName;
                    }

                    String valueStr = null;

                    // 1. Chercher dans le formulaire (POST ou GET query)
                    if (parameterMap.containsKey(fieldName)) {
                        valueStr = parameterMap.get(fieldName)[0];
                    } // 2. Sinon chercher dans les variables d’URL {id}
                    else if (routeParams != null) {
                        String lookup = paramName.startsWith("arg") ? urlParamNames.get(i) : fieldName;
                        valueStr = routeParams.get(lookup);
                    }

                    // Si obligatoire et pas trouvé → erreur claire
                    if (required && (valueStr == null || valueStr.isEmpty())) {
                        throw new ServletException("Paramètre requis manquant : " + fieldName);
                    }

                    // Si non obligatoire et pas trouvé → on met null / valeur par défaut
                    if (valueStr == null) {
                        if (type.isPrimitive()) {
                            throw new ServletException("Paramètre primitif non initialisé : " + fieldName);
                        }
                        args[i] = null;
                        req.setAttribute(fieldName, null);
                        continue;
                    }

                    // Conversion du type
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

                    args[i] = value;
                    req.setAttribute(fieldName, value); // toujours dispo dans la JSP
                }
                result = method.invoke(controller, args);
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
}
