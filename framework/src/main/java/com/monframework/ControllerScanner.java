
package com.monframework;

import com.monframework.annotations.Controller;
import com.monframework.annotations.UrlMapping;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ControllerScanner {

    public static List<Class<?>> getControllers(String packageName) {
        List<Class<?>> controllers = new ArrayList<>();
        String path = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);
            if (resource == null) {
                System.out.println("Aucun répertoire trouvé pour le package : " + packageName);
                return controllers;
            }

            File directory = new File(resource.getFile());
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(Controller.class)) {
                        controllers.add(clazz);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controllers;
    }

    public static void main(String[] args) {
        List<Class<?>> controllers = getControllers("com.monframework.controllers");

        for (Class<?> controller : controllers) {
            System.out.println("Contrôleur trouvé : " + controller.getName());

            // Parcours des méthodes avec @UrlMapping
            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping mapping = method.getAnnotation(UrlMapping.class);
                    System.out.println("   Méthode : " + method.getName() + " → URL : " + mapping.url());
                }
            }
        }
    }
}
