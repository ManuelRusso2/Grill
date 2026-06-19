package utility;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnessioneDB {
    private static DataSource ds;

    // Il blocco static viene eseguito una sola volta all'avvio dell'applicazione
    static {
        try {
            // Inizializza il contesto per cercare le risorse gestite da Tomcat (JNDI)
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            
            // Cerca il DataSource usando lo stesso nome impostato nel context.xml
            ds = (DataSource) envContext.lookup("jdbc/grillDB");
            
        } catch (NamingException e) {
            System.err.println("Errore  nel JNDI Lookup! Controlla context.xml: " + e.getMessage());
        }
    }

    /**
     * Metodo statico richiamabile dai DAO per ottenere una connessione.
     * La connessione viene presa istantaneamente dal Connection Pool di Tomcat.
     * * @return Connection oggetto di connessione attivo verso il database grill
     * @throws SQLException se il database non è raggiungibile o il pool è saturo
     */
    public static Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new SQLException("DataSource non inizializzato");
        }
        return ds.getConnection();
    }
}