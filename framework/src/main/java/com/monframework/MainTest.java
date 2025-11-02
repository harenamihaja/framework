package com.monframework;


import com.monframework.annotations.UrlMapping;
import java.lang.reflect.Method;

public class MainTest {

    @UrlMapping(url="/home")
    public void afficherAccueil() {
        System.out.println("Accueil affiché !");
    }

    @UrlMapping(url="/about")
    public void afficherAPropos() {
        System.out.println("Page À propos affichée !");
    }

    // Méthode principale pour tester
    public static void main(String[] args) {
        try {
            // Crée une instance de la classe actuelle
            MainTest testInstance = new MainTest();

            // Parcourt toutes les méthodes de la classe
            for (Method method : MainTest.class.getDeclaredMethods()) {

                // Vérifie si une méthode a l’annotation @UrlMapping
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping mapping = method.getAnnotation(UrlMapping.class);
                    System.out.println("Méthode trouvée : " + method.getName() + 
                                       " → URL : " + mapping.url());

                    method.invoke(testInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
