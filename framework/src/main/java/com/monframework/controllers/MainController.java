package com.monframework.controllers;

import com.monframework.annotations.Controller;
import com.monframework.annotations.UrlMapping;

@Controller
public class MainController {

    @UrlMapping(url = "/home")
    public void afficherAccueil() {
        System.out.println(" Accueil affiché !");
    }

    @UrlMapping(url = "/about")
    public void afficherAPropos() {
        System.out.println("Page À propos affichée !");
    }

    @UrlMapping(url = "/contact")
    public void afficherContact() {
        System.out.println(" Page Contact affichée !");
    }

    // Méthode sans annotation (ne sera pas détectée)
    public void nonMappee() {
        System.out.println("Cette méthode ne devrait pas apparaître dans la liste.");
    }
}

