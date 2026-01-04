package controller;

import java.util.Map;

import com.monframework.annotations.Controller;
import com.monframework.annotations.PathVariable;
import com.monframework.annotations.PostMapping;
import com.monframework.annotations.RequestParam;
import com.monframework.annotations.UrlMapping;
import com.monframework.models.ModelView;
import com.monframework.annotations.GetMapping;

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

    @PostMapping(value = "/save")
    public ModelView saveWithMap(Map<String, Object> data) {
        ModelView mv = new ModelView("/views/employe-detail.jsp");

        System.out.println("Données reçues : " + data);
        // data contient : {nom=John, age=25} par exemple

        String name = (String) data.get("nom");
        Integer age = null;
        if (data.get("age") instanceof String) {
            age = Integer.valueOf((String) data.get("age"));
        } else if (data.get("age") instanceof Integer) {
            age = (Integer) data.get("age");
        }

        mv.addObject("age", age);
        mv.addObject("name", name);
        return mv;
    }
    // @UrlMapping(url = "/employe/{id}/detail/{name}")
    // public ModelView detailEmploye(@PathVariable("id") String id, @PathVariable("name") String name) {
    //     ModelView mv = new ModelView("/views/employe-detail.jsp");
    //     System.out.println("ato oooooooooooooooooo");
    //     System.out.println("id  " + id);
    //     // mv.addObject("nom", "Rakoto");
    //     // mv.addObject("age", 30);
    //     mv.addObject("id", id);  // Envoie l'id via addObject pour la vue
    //     mv.addObject("name", name);  // Envoie le name via addObject pour la vue
    //     // Ajoutez de la logique ici si besoin, e.g., fetch data par id
    //     return mv;
    // }
    // @UrlMapping(url = "/employe/{id}/detail/{name}")
    // public ModelView detailEmploye(int  id, String name) {
    //     ModelView mv = new ModelView("/views/employe-detail.jsp");
    //     System.out.println("ato oooooooooooooooooo");
    //     System.out.println("id  " + id);
    //     // mv.addObject("nom", "Rakoto");
    //     // mv.addObject("age", 30);
    //     mv.addObject("id", id);  // Envoie l'id via addObject pour la vue
    //     mv.addObject("name", name);  // Envoie le name via addObject pour la vue
    //     return mv ;
    //     // Ajoutez de la logique ici si besoin, e.g., fetch data par id
    //   //  return mv;
    // }
    // @UrlMapping(url = "/save")
    // @PostMapping(value = "/save")
    // public ModelView save1(@RequestParam("nom") String name,
    //         @RequestParam("age") int age) {
    //     // OK même si les noms Java sont différents
    //     ModelView mv = new ModelView("/views/employe-detail.jsp");
    //     System.out.println("Post oooooooooooooooooo");
    //     System.out.println("id  " + age);
    //     mv.addObject("id", age);
    //     mv.addObject("name", name);
    //     return mv;
    // }

    @UrlMapping(url = "/employe/{id}/detail/{name}")
    public ModelView detailEmploye(@PathVariable("id") String id, @PathVariable("name") String name) {
        ModelView mv = new ModelView("/views/employe-detail.jsp");
        System.out.println("ato oooooooooooooooooo");
        System.out.println("id  " + id);
        // mv.addObject("nom", "Rakoto");
        // mv.addObject("age", 30);
        mv.addObject("id", id);  // Envoie l'id via addObject pour la vue
        mv.addObject("name", name);  // Envoie le name via addObject pour la vue

        // Ajoutez de la logique ici si besoin, e.g., fetch data par id
        return mv;
    }

    @UrlMapping(url = "/save-dept")
    public ModelView saveDept(String nom, int id) {
        System.out.println("Nom reçu = " + nom);
        System.out.println("ID reçu = " + id);

        ModelView mv = new ModelView("views/success.jsp");
        mv.addObject("message", "Département sauvegardé : " + nom + " (ID=" + id + ")");
        return mv;
    }

    @UrlMapping(url = "/formDept")
    public ModelView formDept() {
        ModelView mv = new ModelView("views/form_dept.jsp");
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
