package com.monframework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import com.monframework.scanner.Route;
import com.monframework.scanner.ControllerScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            System.out.println("Route enregistrée: " + route.getUrl() + 
                             " -> " + route.getController().getSimpleName() + 
                             "." + route.getMethod().getName() + "()");
        }
        
        System.out.println("=== " + routes.size() + " route(s) trouvée(s) ===");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        chercherRessource(req, resp);
    }

    private void chercherRessource(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Vérifier si une route correspond à ce path
        if (routeMap.containsKey(path)) {
            Route route = routeMap.get(path);
            executerRoute(route, req, resp);
            return;
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
            // Créer une instance du controller
            Object controllerInstance = route.getController().getDeclaredConstructor().newInstance();

            // Récupérer la méthode
            Method method = route.getMethod();

            // Invoquer la méthode
            Object result = method.invoke(controllerInstance);

            // Gérer le résultat
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();

            out.println("<html><body>");
            out.println("<h2>Route trouvée!</h2>");
            out.println("<p><b>URL:</b> " + route.getUrl() + "</p>");
            out.println("<p><b>Controller:</b> " + route.getController().getSimpleName() + "</p>");
            out.println("<p><b>Méthode:</b> " + route.getMethod().getName() + "()</p>");
            out.println("<hr>");
            out.println("<h3>Résultat:</h3>");
            out.println("<p>" + (result != null ? result.toString() : "void") + "</p>");
            out.println("</body></html>");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>Erreur 500 - Erreur interne</h1>");
            out.println("<p>Erreur lors de l'exécution du controller:</p>");
            out.println("<pre>" + e.getMessage() + "</pre>");
            e.printStackTrace();
        }
    }
}
