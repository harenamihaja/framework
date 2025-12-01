package com.monframework.exceptions;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;

public class MethodNotAllowedException extends RuntimeException {
    private final Set<String> allowedMethods;

    public MethodNotAllowedException(Set<String> allowedMethods) {
        super("HTTP method not allowed");
        this.allowedMethods = allowedMethods;
    }

    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    public int getStatusCode() {
        return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
}