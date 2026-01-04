package model;

import java.util.ArrayList;
import java.util.List;

public class Employe {
    private int id;
    private String nom;
    private int age;
public Employe() {
    }
    // getters et setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }


    public List<Employe> getAllEmploye(){
        List<Employe> employes = new ArrayList<>();
        Employe e1 = new Employe();
        e1.setNom("Alice");
        e1.setAge(30);
        employes.add(e1);

        Employe e2 = new Employe();
        e2.setNom("Bob");
        e2.setAge(25);
        employes.add(e2);

        return employes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

