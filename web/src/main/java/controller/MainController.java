package controller;

import com.monframework.annotations.Controller;
import com.monframework.annotations.UrlMapping;
import com.monframework.models.ModelView;

@Controller
public class MainController {

    // @UrlMapping(url = "/home")
    // public String afficherAccueil() {
    //     System.out.println(" Accueil affiché !");
    //     return "<h1>Hello depuis la méthode Home !</h1>";
    // }
    @UrlMapping(url = "/home")
    public ModelView afficherAccueil() {
        ModelView mv = new ModelView();
        //    mv.setView("/WEB-INF/views/home.jsp");
        mv.setView("home.jsp");

        return mv;
    }

    @UrlMapping(url = "/test")
    public ModelView afficherTest() {
        ModelView mv = new ModelView();
        //    mv.setView("/WEB-INF/views/home.jsp");
        mv.setView("views/test.jsp");

        return mv;
    }

    @UrlMapping(url = "/employe")
    public ModelView listEmployes() {
        ModelView mv = new ModelView("views/employes.jsp");

        mv.addObject("nom", "Rakoto");
        mv.addObject("age", 30);
        return mv;
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
