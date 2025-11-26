package com.monframework.scanner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

    private String url;
    private Method method;
    private Class<?> controller;

    private Pattern pattern;
    private List<String> paramNames = new ArrayList<>();

    // public Pattern getPattern() {
    //     return pattern;
    // }

    // public void setPattern(Pattern pattern) {
    //     this.pattern = pattern;
    // }

    public List<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }

    public Route(String url, Method method, Class<?> controller) {
        this.url = url;
        this.method = method;
        this.controller = controller;
        // Convertir /employe/{id} en /employe/([^/]+)
        // String regex = url.replaceAll("\\{[^/]+}", "([^/]+)");
        // this.pattern = Pattern.compile(regex);
        Matcher nameMatcher = Pattern.compile("\\{([^/]+)\\}").matcher(url);
        while (nameMatcher.find()) {
            paramNames.add(nameMatcher.group(1));
        }

        // Convert URL template to regex (e.g., /employe/{id} -> ^/employe/([^/]+)$)
        String regex = url.replaceAll("\\{[^/]+\\}", "([^/]+)");
        this.pattern = Pattern.compile("^" + regex + "$");
    }
    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getUrl() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getController() {
        return controller;
    }

    @Override
    public String toString() {
        return "Route{url='" + url + "', method=" + method.getName()
                + ", controller=" + controller.getSimpleName() + "}";
    }
}
