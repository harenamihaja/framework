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

        String controllerPackage = getInitParameter("controllerPackage");
        if (controllerPackage == null) {
            controllerPackage = "controller";
        }

        System.out.println("=== Scanning package: " + controllerPackage + " ===");

        List<Route> routes = ControllerScanner.getRoutes(controllerPackage);

        for (Route route : routes) {
            String url = route.getUrl();
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

        if (routeMap.containsKey(path)) {
            List<Route> candidates = routeMap.get(path);
            matchedRoute = findByHttpMethod(candidates, httpMethod);
            if (matchedRoute == null) {
                Set<String> allowed = candidates.stream()
                        .map(Route::getHttpMethod)
                        .collect(java.util.stream.Collectors.toSet());
                throw new MethodNotAllowedException(allowed);
            }
        }

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
                            Set<String> allowed = candidates.stream()
                                    .filter(r -> r.getPattern().matcher(path).matches())
                                    .map(Route::getHttpMethod)
                                    .collect(java.util.stream.Collectors.toSet());
                            throw new MethodNotAllowedException(allowed);
                        }
                    }
                }
                if (matchedRoute != null) break;
            }
        }

        if (matchedRoute != null) {
            if (routeParams != null) {
                req.setAttribute("routeParams", routeParams);
            }
            executerRoute(matchedRoute, req, resp);
            return;
        }

        if ("/".equals(path)) {
            showRouteList(req, resp);
            return;
        }

        if (getServletContext().getResource(path) != null) {
            getServletContext().getNamedDispatcher("default").forward(req, resp);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().println("<h1>404 - Not Found</h1><p>URL introuvable : " + path + "</p>");
    }

    private void executerRoute(Route route, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Object controller = route.getController().getDeclaredConstructor().newInstance();
            Method method = route.getMethod();

            Map<String, String[]> parameterMap = req.getParameterMap();
            @SuppressWarnings("unchecked")
            Map<String, String> routeParams = (Map<String, String>) req.getAttribute("routeParams");
            List<String> urlParamNames = route.getParamNames();

            Object result;

            if (method.getParameterCount() == 0) {
                result = method.invoke(controller);
            } else {
                Parameter[] parameters = method.getParameters();

                // Détection des types de paramètres
                boolean hasMapParameter = false;
                int mapParamIndex = -1;
                boolean hasModelObjectParameter = false;
                int modelParamIndex = -1;
                Class<?> modelClass = null;

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];

                    // Cas Map<String, Object>
                    if (Map.class.isAssignableFrom(param.getType()) &&
                        "java.util.Map<java.lang.String, java.lang.Object>".equals(param.getParameterizedType().getTypeName())) {
                        hasMapParameter = true;
                        mapParamIndex = i;
                    }
                    // Cas objet modèle (package model)
                    else if (isModelClass(param.getType())) {
                        if (hasModelObjectParameter) {
                            throw new ServletException("Un seul paramètre objet modèle autorisé par méthode.");
                        }
                        hasModelObjectParameter = true;
                        modelParamIndex = i;
                        modelClass = param.getType();
                    }
                }

                Object[] args = new Object[parameters.length];

                // === CAS 1 : Map<String, Object> ===
                if (hasMapParameter) {
                    Map<String, Object> requestData = new HashMap<>();
                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        requestData.put(key, values.length == 1 ? values[0] : values);
                    }
                    if (routeParams != null) {
                        requestData.putAll(routeParams);
                    }
                    args[mapParamIndex] = requestData;
                    for (int i = 0; i < args.length; i++) {
                        if (i != mapParamIndex) args[i] = null;
                    }
                    result = method.invoke(controller, args);

                }
                // === CAS 2 : Objet modèle (nouvelle fonctionnalité) ===
                else if (hasModelObjectParameter) {
                    Object modelObject = buildModelObject(modelClass, parameterMap, routeParams);
                    args[modelParamIndex] = modelObject;
                    for (int i = 0; i < args.length; i++) {
                        if (i != modelParamIndex) args[i] = null;
                    }
                    result = method.invoke(controller, args);

                }
                // === CAS 3 : Comportement classique (@RequestParam, PathVariable) ===
                else {
                    Object[] argsClassic = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter param = parameters[i];
                        Class<?> type = param.getType();
                        String paramName = param.getName();

                        String fieldName = paramName;
                        boolean required = true;

                        com.monframework.annotations.RequestParam rp = param.getAnnotation(com.monframework.annotations.RequestParam.class);
                        if (rp != null) {
                            fieldName = rp.value().isEmpty() ? paramName : rp.value();
                            required = rp.required();
                        }

                        String valueStr = null;
                        if (parameterMap.containsKey(fieldName)) {
                            valueStr = parameterMap.get(fieldName)[0];
                        } else if (routeParams != null) {
                            String lookup = urlParamNames.size() > i ? urlParamNames.get(i) : fieldName;
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
                            continue;
                        }

                        Object value = switch (type.getSimpleName()) {
                            case "int", "Integer" -> Integer.parseInt(valueStr);
                            case "long", "Long" -> Long.parseLong(valueStr);
                            case "double", "Double" -> Double.parseDouble(valueStr);
                            case "boolean", "Boolean" -> Boolean.parseBoolean(valueStr);
                            default -> valueStr;
                        };

                        argsClassic[i] = value;
                        req.setAttribute(fieldName, value);
                    }
                    result = method.invoke(controller, argsClassic);
                }
            }

            // Gestion du retour
            if (result instanceof ModelView mv) {
                String view = mv.getView();
                if (view == null || view.isEmpty()) {
                    throw new ServletException("ModelView sans vue définie");
                }
                mv.getMapData().forEach(req::setAttribute);
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

    // ==================== MÉTHODES UTILITAIRES POUR LE BINDING D'OBJETS MODÈLE ====================

    private boolean isModelClass(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null && pkg.getName().startsWith("model");
    }

    private Object buildModelObject(Class<?> targetClass, Map<String, String[]> parameterMap, Map<String, String> routeParams)
            throws Exception {

        Object instance = targetClass.getDeclaredConstructor().newInstance();

        Map<String, String> allParams = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getValue().length > 0) {
                allParams.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        if (routeParams != null) {
            allParams.putAll(routeParams);
        }

        String rootName = targetClass.getSimpleName().toLowerCase();

        Map<String, String> relevantParams = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(rootName + ".")) {
                String subKey = entry.getKey().substring(rootName.length() + 1);
                relevantParams.put(subKey, entry.getValue());
            }
        }

        if (!relevantParams.isEmpty()) {
            applyValuesToObject(instance, relevantParams);
        }

        return instance;
    }

    private void applyValuesToObject(Object obj, Map<String, String> params) throws Exception {
        if (obj == null || params.isEmpty()) return;

        Class<?> clazz = obj.getClass();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String keyPath = entry.getKey();
            String valueStr = entry.getValue();
            if (valueStr == null || valueStr.isEmpty()) continue;

            String[] parts = keyPath.split("\\.", 2);
            String property = parts[0];
            String subPath = parts.length > 1 ? parts[1] : null;

            String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            Method setter = findSetter(clazz, setterName);

            if (setter == null) continue;

            Class<?> paramType = setter.getParameterTypes()[0];

            if (subPath != null) {
                Object nestedObj = setter.invoke(obj);
                if (nestedObj == null) {
                    nestedObj = paramType.getDeclaredConstructor().newInstance();
                    setter.invoke(obj, nestedObj);
                }
                Map<String, String> subMap = new HashMap<>();
                subMap.put(subPath, valueStr);
                applyValuesToObject(nestedObj, subMap);
            } else {
                Object value = convertStringToType(valueStr, paramType);
                setter.invoke(obj, value);
            }
        }
    }

    private Method findSetter(Class<?> clazz, String setterName) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    private Object convertStringToType(String value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    // ==================== FIN DES MÉTHODES UTILITAIRES ====================

    private void showRouteList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<h1>Framework Spring-like</h1>");
        out.println("<h2>Routes disponibles :</h2>");
        out.println("<ul style='font-family: monospace; line-height: 1.8;'>");

        routeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String url = entry.getKey();
                    List<Route> routes = entry.getValue();

                    routes.forEach(r -> {
                        String methodColor = switch (r.getHttpMethod()) {
                            case "GET" -> "#28a745";
                            case "POST" -> "#dc3545";
                            case "ANY" -> "#6f42c1";
                            default -> "#000000";
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