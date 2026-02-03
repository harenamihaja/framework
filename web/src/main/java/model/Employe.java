package model;

import java.util.ArrayList;
import java.util.List;

public class Employe {

    private int id;
    private String nom;
    private int age;
    private List<Employe> allEmploye;

    public Employe() {
    }

    // getters et setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public  List<Employe> getAllEmploye() {
        List<Employe> employes = new ArrayList<>();
        Employe e1 = new Employe();
        e1.setId(1);
        e1.setNom("Alice");
        e1.setAge(30);
        employes.add(e1);

        Employe e2 = new Employe();
        e2.setId(2);
        e2.setNom("Bob");
        e2.setAge(25);
        employes.add(e2);
        this.setAllEmploye(employes);
        return allEmploye;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAllEmploye(List<Employe> allEmploye) {
        this.allEmploye = allEmploye;
    }
}
