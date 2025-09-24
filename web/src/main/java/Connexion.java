// package connexion;
import java.sql.*;
public class Connexion{
    Connection connexion;
    Statement s;
    // private final static String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    protected String URL = "jdbc:mysql://localhost:3306/db_s2_ETU003162";
    // private static final String USER="lmd";
    protected String USER ="ETU003162";
    // private static final String password="lmd";
    protected String password="rkFbh9Nv";

    public Connexion() throws  Exception
    {
        try {
            System.out.println("connexion reussie");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé avec succès !");
            Connection co= DriverManager.getConnection(this.URL, this.USER,  this.password);
            this.connexion=co;
            this.s=co.createStatement();
        } catch (Exception ex) {
           throw ex;
        }
      
    }
    public void closeAll() throws SQLException
    {
        this.connexion.close();
        this.s.close();
    }
    public Connection getConnexion()
    {
        return this.connexion;
    }
    public Statement getStatement()
    {
        return this.s;
    }

}