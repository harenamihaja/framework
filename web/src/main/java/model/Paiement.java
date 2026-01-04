package model;

public class Paiement {
    int id;
    Employe employe;
    double montant;
    public Paiement(int id, Employe employe, double montant) {
        this.id = id;
        this.employe = employe;
        this.montant = montant;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Employe getIdEmploye() {
        return employe;
    }
    public void setIdEmploye(Employe idEmploye) {
        this.employe = idEmploye;
    }
    public double getMontant() {
        return montant;
    }
    public void setMontant(double montant) {
        this.montant = montant;
    }
}
