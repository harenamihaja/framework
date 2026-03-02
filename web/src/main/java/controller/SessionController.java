package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.monframework.annotations.Controller;
import com.monframework.annotations.RequestParam;
import com.monframework.annotations.Session;
import com.monframework.annotations.UrlMapping;
import com.monframework.models.ModelView;

@Controller
public class SessionController {

    // ───────────────────────────────────────────────
    // 1. Formulaire d'ajout de produit
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/session/form")
    public ModelView afficherForm() {
        ModelView mv = new ModelView();
        mv.setView("/views/session/form-produit.jsp");
        mv.addObject("titre", "Ajouter un produit au panier");
        return mv;
    }

    // ───────────────────────────────────────────────
    // 2. Ajouter un produit en session (panier)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/session/ajouter")
    public ModelView ajouterProduit(
            @RequestParam("nom") String nom,
            @RequestParam("prix") Double prix,
            @Session Map<String, Object> session) {

        System.out.println("=== Ajout en session ===");
        System.out.println("Session avant: " + session);

        // Récupération ou création de la liste en session
        @SuppressWarnings("unchecked")
        List<String> panier = (List<String>) session.getOrDefault("panier", new ArrayList<>());

        String produit = nom + " - " + prix + "€";
        panier.add(produit);

        // Mise à jour en session
        session.put("panier", panier);
        session.put("nombreArticles", panier.size());
        session.put("dernier", nom);

        System.out.println("Session après: " + session);
        System.out.println("=== Panier mis à jour ===");

        ModelView mv = new ModelView();
        mv.setView("redirect:/session/afficher");
        return mv;
    }

    // ───────────────────────────────────────────────
    // 3. Afficher le panier (données de la session)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/session/afficher")
    public ModelView afficherPanier(@Session Map<String, Object> session) {
        System.out.println("=== Affichage panier ===");
        System.out.println("Session: " + session);

        ModelView mv = new ModelView();
        mv.setView("../views/session/panier.jsp");

        @SuppressWarnings("unchecked")
        List<String> panier = (List<String>) session.get("panier");
        Integer nombreArticles = (Integer) session.get("nombreArticles");
        String dernier = (String) session.get("dernier");

        if (panier != null && !panier.isEmpty()) {
            mv.addObject("panier", panier);
            mv.addObject("nombreArticles", nombreArticles);
            mv.addObject("dernier", dernier);
            mv.addObject("message", "Panier : " + panier.size() + " article(s)");
        } else {
            mv.addObject("message", "Le panier est vide");
        }

        return mv;
    }

    // ───────────────────────────────────────────────
    // 4. Vider le panier (supprimer de la session)
    // ───────────────────────────────────────────────
    @UrlMapping(url = "/session/vider")
    public ModelView viderPanier(@Session Map<String, Object> session) {
        session.remove("panier");
        session.remove("nombreArticles");
        session.remove("dernier");

        System.out.println("=== Panier vidé ===");

        ModelView mv = new ModelView();
        mv.setView("redirect:/session/afficher");
        return mv;
    }
}
