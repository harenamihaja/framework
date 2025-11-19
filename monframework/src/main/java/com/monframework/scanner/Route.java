package com.monframework.scanner;

import java.lang.reflect.Method;

public class Route {
    private String url;
    private Method method;
    private Class<?> controller;

    public Route(String url, Method method, Class<?> controller) {
        this.url = url;
        this.method = method;
        this.controller = controller;
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
        return "Route{url='" + url + "', method=" + method.getName() + 
               ", controller=" + controller.getSimpleName() + "}";
    }
}