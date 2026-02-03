package com.monframework;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import com.monframework.scanner.Route;
import com.monframework.models.ModelView;
import com.monframework.scanner.ControllerScanner;
import com.monframework.annotations.JsonResponse;
import com.monframework.annotations.FileUpload;
import com.monframework.models.UploadedFile;
import com.monframework.exceptions.MethodNotAllowedException;
import com.monframework.annotations.Session; // Importez la nouvelle annotation

import java.util.ArrayList;
import java.util.Enumeration; // Ajouté pour HttpSession
import java.util.HashMap;
import java.util.HashSet; // Ajouté pour initialKeys
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.regex.Matcher;
import jakarta.servlet.http.HttpSession; // Ajouté pour HttpSession

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class sprint11 extends HttpServlet {

    private Map<String, List<Route>> routeMap = new HashMap<>();
    private static final String UPLOAD_DIR = "uploads";

    
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
                if (matchedRoute != null) {
                    break;
                }
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

            // === INVOQUER LA MÉTHODE DU CONTROLLER ===
            if (method.getParameterCount() == 0) {
                result = method.invoke(controller);
            } else {
                Parameter[] parameters = method.getParameters();

                boolean hasRequestMap = false;
                int requestMapIndex = -1;
                boolean hasSessionMap = false;
                int sessionMapIndex = -1;
                boolean hasModelObject = false;
                int modelIndex = -1;

                // Détection des types spéciaux de paramètres
                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];

                    if (Map.class.isAssignableFrom(param.getType())
                            && "java.util.Map<java.lang.String, java.lang.Object>".equals(param.getParameterizedType().getTypeName())) {
                        if (param.isAnnotationPresent(Session.class)) {
                            if (hasSessionMap) {
                                throw new ServletException("Un seul paramètre @Session autorisé par méthode.");
                            }
                            hasSessionMap = true;
                            sessionMapIndex = i;
                        } else {
                            if (hasRequestMap) {
                                throw new ServletException("Un seul paramètre Map<String, Object> (request) autorisé par méthode.");
                            }
                            hasRequestMap = true;
                            requestMapIndex = i;
                        }
                    } else if (isModelClass(param.getType())) {
                        if (hasModelObject) {
                            throw new ServletException("Un seul paramètre objet modèle autorisé par méthode.");
                        }
                        hasModelObject = true;
                        modelIndex = i;
                    }
                }

                // Construction des arguments
                Object[] args = new Object[parameters.length];
                Set<String> initialSessionKeys = null;

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    Class<?> type = param.getType();

                    if (param.isAnnotationPresent(Session.class)) {
                        // Gestion @Session Map<String, Object>
                        Map<String, Object> sessionMap = new HashMap<>();
                        HttpSession session = req.getSession(true); // Créer la session si elle n'existe pas
                        Enumeration<String> attrNames = session.getAttributeNames();
                        initialSessionKeys = new HashSet<>();
                        while (attrNames.hasMoreElements()) {
                            String name = attrNames.nextElement();
                            Object val = session.getAttribute(name);
                            sessionMap.put(name, val);
                            initialSessionKeys.add(name);
                        }
                        args[i] = sessionMap;
                        continue;
                    } else if (Map.class.isAssignableFrom(type)
                            && "java.util.Map<java.lang.String, java.lang.Object>".equals(param.getParameterizedType().getTypeName())) {
                        // Gestion Map<String, Object> pour request data
                        Map<String, Object> requestData = new HashMap<>();
                        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                            String key = entry.getKey();
                            String[] values = entry.getValue();
                            requestData.put(key, values.length == 1 ? values[0] : values);
                        }
                        if (routeParams != null) {
                            requestData.putAll(routeParams);
                        }
                        args[i] = requestData;
                        continue;
                    } else if (isModelClass(type)) {
                        // Gestion binding objet modèle
                        args[i] = buildModelObject(type, parameterMap, routeParams);
                        continue;
                    } else {
                        // Gestion paramètres classiques (@RequestParam, @PathVariable, @FileUpload, etc.)
                        String paramName = param.getName();

                        if (type == HttpServletRequest.class) {
                            args[i] = req;
                            continue;
                        }

                        if (type == HttpServletResponse.class) {
                            args[i] = resp;
                            continue;
                        }

                        // Gestion UploadedFile unique
                        if (type == UploadedFile.class) {
                            FileUpload fileAnnotation = param.getAnnotation(FileUpload.class);
                            String fieldName = (fileAnnotation != null && !fileAnnotation.value().isEmpty())
                                    ? fileAnnotation.value()
                                    : paramName;

                            Part part = req.getPart(fieldName);

                            if (part == null || part.getSize() == 0) {
                                if (fileAnnotation != null && fileAnnotation.required()) {
                                    throw new ServletException("Fichier requis manquant : " + fieldName);
                                }
                                args[i] = null;
                            } else {
                                args[i] = new UploadedFile(part);
                            }
                            continue;
                        }

                        // Gestion List<UploadedFile>
                        if (type == List.class && param.getParameterizedType().getTypeName().contains("UploadedFile")) {
                            FileUpload fileAnnotation = param.getAnnotation(FileUpload.class);
                            String fieldName = (fileAnnotation != null && !fileAnnotation.value().isEmpty())
                                    ? fileAnnotation.value()
                                    : paramName;

                            Collection<Part> parts = req.getParts();
                            List<UploadedFile> files = new ArrayList<>();

                            for (Part part : parts) {
                                if (part.getName().equals(fieldName) && part.getSize() > 0) {
                                    files.add(new UploadedFile(part));
                                }
                            }

                            if (files.isEmpty() && fileAnnotation != null && fileAnnotation.required()) {
                                throw new ServletException("Fichiers requis manquants : " + fieldName);
                            }

                            args[i] = files;
                            continue;
                        }

                        // Logique pour paramètres scalaires
                        String fieldName = paramName;
                        boolean required = true;

                        com.monframework.annotations.RequestParam rp =
                                param.getAnnotation(com.monframework.annotations.RequestParam.class);
                        if (rp != null) {
                            fieldName = rp.value().isEmpty() ? paramName : rp.value();
                            required = rp.required();
                        }

                        String valueStr = null;
                        String lookup = paramName;
                        if (urlParamNames != null && i < urlParamNames.size()) {
                            lookup = urlParamNames.get(i);
                        } else {
                            lookup = fieldName;
                        }

                        if (routeParams != null && routeParams.containsKey(lookup)) {
                            valueStr = routeParams.get(lookup);
                        } else if (parameterMap.containsKey(fieldName)) {
                            valueStr = parameterMap.get(fieldName)[0];
                        }

                        if (required && (valueStr == null || valueStr.isEmpty())) {
                            throw new ServletException("Paramètre requis manquant : " + fieldName);
                        }

                        if (valueStr == null) {
                            if (type.isPrimitive()) {
                                throw new ServletException("Paramètre primitif non initialisé : " + fieldName);
                            }
                            args[i] = null;
                            continue;
                        }

                        Object value = null;
                        String typeName = type.getSimpleName();
                        switch (typeName) {
                            case "int":
                            case "Integer":
                                value = Integer.parseInt(valueStr);
                                break;
                            case "long":
                            case "Long":
                                value = Long.parseLong(valueStr);
                                break;
                            case "double":
                            case "Double":
                                value = Double.parseDouble(valueStr);
                                break;
                            case "boolean":
                            case "Boolean":
                                value = Boolean.parseBoolean(valueStr);
                                break;
                            default:
                                value = valueStr;
                        }

                        args[i] = value;
                        req.setAttribute(fieldName, value);
                    }
                }

                // Invocation de la méthode
                result = method.invoke(controller, args);

                // Persistance des changements de session si @Session présent
                if (hasSessionMap) {
                    Map<String, Object> modifiedMap = (Map<String, Object>) args[sessionMapIndex];
                    HttpSession session = req.getSession();
                    if (initialSessionKeys != null) {
                        for (String key : initialSessionKeys) {
                            if (!modifiedMap.containsKey(key)) {
                                session.removeAttribute(key);
                            }
                        }
                    }
                    for (Map.Entry<String, Object> entry : modifiedMap.entrySet()) {
                        session.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
            }

            // === GESTION DU RETOUR DE LA MÉTHODE ===
            if (result instanceof ModelView mv) {
                String view = mv.getView();
                if (view == null || view.isEmpty()) {
                    throw new ServletException("ModelView sans vue définie");
                }
                mv.getMapData().forEach(req::setAttribute);
                RequestDispatcher dispatcher = req.getRequestDispatcher(view);
                dispatcher.forward(req, resp);

            } else if (result instanceof String str) {
                resp.setContentType("text/html; charset=UTF-8");
                resp.getWriter().println(str);

            } else if (result != null) {
                // === RETOUR JSON avec @JsonResponse ===
                if (method.isAnnotationPresent(JsonResponse.class)) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    PrintWriter out = resp.getWriter();
                    out.print(toJson(result));
                    out.flush();
                } else {
                    throw new ServletException("Type de retour non supporté sans @JsonResponse : " + result.getClass().getName()
                            + ". Utilisez ModelView, String ou annotez la méthode avec @JsonResponse.");
                }

            } else {
                // result == null (void)
                if (method.isAnnotationPresent(JsonResponse.class)) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    throw new ServletException("Méthode retourne void sans @JsonResponse. Attendu : ModelView, String ou objet annoté @JsonResponse.");
                }
            }

        } catch (Exception e) {
            // Gestion centralisée des erreurs
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
        if (obj == null || params == null || params.isEmpty()) {
            return;
        }

        Class<?> clazz = obj.getClass();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String keyPath = entry.getKey();
            String valueStr = entry.getValue();

            if (valueStr == null || valueStr.trim().isEmpty()) {
                continue;
            }

            valueStr = valueStr.trim();

            String[] parts = keyPath.split("\\.", 2);
            String property = parts[0];
            String subPath = parts.length > 1 ? parts[1] : null;

            String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            Method setter = findSetter(clazz, setterName);

            if (setter == null) {
                System.out.println("Warning: Setter non trouvé pour '" + property + "' dans " + clazz.getSimpleName());
                continue;
            }

            Class<?> paramType = setter.getParameterTypes()[0];

            if (subPath != null) {
                // === CAS IMBRIQUÉ ===
                String getterName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
                Method getter = findGetter(clazz, getterName);

                Object nestedObj = null;
                if (getter != null) {
                    nestedObj = getter.invoke(obj);
                }

                if (nestedObj == null) {
                    nestedObj = paramType.getDeclaredConstructor().newInstance();
                    setter.invoke(obj, new Object[]{nestedObj});
                }

                Map<String, String> subMap = new HashMap<>();
                subMap.put(subPath, valueStr);
                applyValuesToObject(nestedObj, subMap);

            } else {
                // === CAS SIMPLE ===
                try {
                    Object value = convertStringToType(valueStr, paramType);

                    if (paramType.isPrimitive() && value instanceof Number) {
                        if (paramType == int.class) {
                            setter.invoke(obj, new Object[]{((Number) value).intValue()});
                        } else if (paramType == double.class) {
                            setter.invoke(obj, new Object[]{((Number) value).doubleValue()});
                        } else if (paramType == long.class) {
                            setter.invoke(obj, new Object[]{((Number) value).longValue()});
                        } else if (paramType == float.class) {
                            setter.invoke(obj, new Object[]{((Number) value).floatValue()});
                        } else if (paramType == short.class) {
                            setter.invoke(obj, new Object[]{((Number) value).shortValue()});
                        } else if (paramType == byte.class) {
                            setter.invoke(obj, new Object[]{((Number) value).byteValue()});
                        } else if (paramType == boolean.class) {
                            setter.invoke(obj, new Object[]{value});
                        } else {
                            setter.invoke(obj, new Object[]{value});
                        }
                    } else {
                        setter.invoke(obj, new Object[]{value});
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Erreur de conversion : '" + valueStr + "' → " + paramType.getSimpleName() + " (" + property + ")");
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'appel au setter " + setterName + " : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private Method findGetter(Class<?> clazz, String getterName) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(getterName) && m.getParameterCount() == 0) {
                return m;
            }
        }
        return null;
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
        if (value == null || value.isEmpty()) {
            return null;
        }

        String trimmed = value.trim();

        if (targetType == String.class) {
            return trimmed;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.valueOf(trimmed);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.valueOf(trimmed);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.valueOf(trimmed);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.valueOf(trimmed);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(trimmed);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.valueOf(trimmed);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.valueOf(trimmed);
        }

        return trimmed;
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

    private String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }

        // === CAS SPÉCIAL : List → tableau JSON direct ===
        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }

        // === CAS SPÉCIAL : Tableau Java → tableau JSON direct ===
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(toJson(java.lang.reflect.Array.get(obj, i)));
            }
            sb.append("]");
            return sb.toString();
        }

        // === CAS SPÉCIAL : Types primitifs ou wrappers connus ===
        if (obj instanceof String str) {
            return "\"" + escapeJsonString(str) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        // === CAS GÉNÉRAL : Objet POJO → objet JSON { ... } ===
        StringBuilder sb = new StringBuilder("{");
        java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;

        for (java.lang.reflect.Field field : fields) {
            int modifiers = field.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(modifiers)
                    || java.lang.reflect.Modifier.isTransient(modifiers)
                    || field.isSynthetic()) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) {
                    continue;
                }

                if (!first) {
                    sb.append(",");
                }
                first = false;

                sb.append("\"").append(field.getName()).append("\":");
                sb.append(toJson(value));

            } catch (IllegalAccessException e) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(field.getName()).append("\":\"<access_error>\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}