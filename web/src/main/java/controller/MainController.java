package controller;

import com.monframework.annotations.Controller;
import com.monframework.annotations.UrlMapping;

@Controller
public class MainController {

    @UrlMapping(url = "/home")
    public String afficherAccueil() {
        System.out.println(" Accueil affiché !");
        return "<h1>Hello depuis la méthode Home !</h1>";

    }

    @UrlMapping(url = "/about")
    public String afficherAPropos() {
        System.out.println("Page À propos affichée !");
        return "<h1>Page À propos affichée !</h1>";
    }

    @UrlMapping(url = "/contact")
    public String afficherContact() {
        System.out.println(" Page Contact affichée !");
        return "<h1>Page Contact affichée !</h1>";
    }

    // Méthode sans annotation (ne sera pas détectée)
    public void nonMappee() {
        System.out.println("Cette méthode ne devrait pas apparaître dans la liste.");
    }
}
