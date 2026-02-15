package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.monframework.annotations.Controller;
import com.monframework.annotations.PathVariable;
import com.monframework.annotations.PostMapping;
import com.monframework.annotations.RequestParam;
import com.monframework.annotations.Session;
import com.monframework.annotations.JsonResponse;

import com.monframework.annotations.UrlMapping;
import com.monframework.models.ModelView;
import model.Employe;
import model.Paiement;
import com.monframework.annotations.GetMapping;
import com.monframework.annotations.JsonResponse;

@Controller
public class SessionController {

    // Simulation d'une "base de données" via la méthode du modèle
    private List<Employe> getAllEmployesFromModel() {
        Employe dummy = new Employe(); // juste pour appeler la méthode
        return dummy.getAllEmploye();   // retourne la liste statique simulée
    }
// @UrlMapping(url = "/formTestEmploye")
//     public ModelView afficherFormEmploye() {
//         ModelView mv = new ModelView();
//         List<Employe> employes = new Employe().getAllEmploye();
//         //   mv.addObject("employes", employes);
//         //    mv.setView("/WEB-INF/views/home.jsp");
//         mv.setView("views/session/form_employe.jsp");

//         return mv;
//     }
    // ───────────────────────────────────────────────
    // 1. Liste de tous les employés
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/employesTest")
    public ModelView listeEmployes() {
        ModelView mv = new ModelView();
        mv.setView("views/session/employes.jsp");

        List<Employe> employes = getAllEmployesFromModel();
        System.out.println("Liste des employés : " + employes.get(0).getNom());
            System.out.println("Liste des employés : " + employes.get(1).getNom());

        mv.addObject("employes", employes);
        mv.addObject("titre", "Liste des employés");

        return mv;
    }

    // ───────────────────────────────────────────────
    // 2. Détail d'un employé (via id en paramètre)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/detail")
    public ModelView detailEmploye(@RequestParam("id") Integer id) {
        ModelView mv = new ModelView();
        mv.setView("views/session/detail-employe.jsp");

        List<Employe> all = getAllEmployesFromModel();

        Employe found = all.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(null);

        if (found != null) {
            mv.addObject("employe", found);
        } else {
            mv.addObject("messageErreur", "Employé avec id " + id + " introuvable");
        }

        return mv;
    }

    // ───────────────────────────────────────────────
    // 3. Formulaire d'ajout (affiche le form)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/ajouterEmploye")
    public ModelView afficherFormAjout() {
        ModelView mv = new ModelView();
        mv.setView("views/session/ajout-employe.jsp");
        mv.addObject("titre", "Ajouter un employé");
        return mv;
    }

    // ───────────────────────────────────────────────x
    // 4. Ajouter un employé en session (panier sélection)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/ajouter")
    public ModelView ajouterEmployeEnSession(
            @RequestParam("nom") String nom,
            @RequestParam("age") Integer age,
            @Session Map<String, Object> session) {

        ModelView mv = new ModelView();
        mv.setView("redirect:/employe/selectionnes");

        // Création de l'employé
        Employe nouvelEmploye = new Employe();
        nouvelEmploye.setId((int) (Math.random() * 10000)); // simulation auto-incr
        nouvelEmploye.setNom(nom);
        nouvelEmploye.setAge(age);

        // Récupération ou création de la liste en session
        @SuppressWarnings("unchecked")
        List<Employe> selection = (List<Employe>) session.getOrDefault("selectionEmployes", new ArrayList<>());

        selection.add(nouvelEmploye);

        // Mise à jour en session
        session.put("selectionEmployes", selection);
        session.put("dernierAjout", nom + " (" + age + " ans)");

        return mv;
    }

    // ───────────────────────────────────────────────
    // 5. Voir les employés sélectionnés (en session)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/employe/selectionnes")
    public ModelView voirSelection(@Session Map<String, Object> session) {
        ModelView mv = new ModelView();
        mv.setView("selection-employes.jsp");

        @SuppressWarnings("unchecked")
        List<Employe> selection = (List<Employe>) session.get("selectionEmployes");

        if (selection != null && !selection.isEmpty()) {
            mv.addObject("selection", selection);
            mv.addObject("message", "Employés sélectionnés : " + selection.size());
        } else {
            mv.addObject("message", "Aucun employé sélectionné pour le moment.");
        }

        return mv;
    }

    // ───────────────────────────────────────────────
    // 6. Vider la sélection (supprimer de la session)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/employe/vider-selection")
    public ModelView viderSelection(@Session Map<String, Object> session) {
        session.remove("selectionEmployes");
        session.remove("dernierAjout");

        ModelView mv = new ModelView();
        mv.setView("redirect:/employe/selectionnes");
        return mv;
    }
}
