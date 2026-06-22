import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAOImpl implements UtenteDAO {

    // QUERY SQL (Definite come costanti private per rendere il codice pulito)
    private static final String INSERT_UTENTE = "INSERT INTO utenti (nome, cognome, email, password_hash, ruolo) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_EMAIL = "SELECT id_utente, nome, cognome, email, password_hash, ruolo FROM utenti WHERE email = ?";
    private static final String SELECT_BY_ID = "SELECT id_utente, nome, cognome, email, password_hash, ruolo FROM utenti WHERE id_utente = ?";
    private static final String SELECT_ALL_CLIENTI = "SELECT id_utente, nome, cognome, email, password_hash, ruolo FROM utenti WHERE ruolo = 'CLIENTE'";

    @Override
    public void doSave(Utente utente) throws SQLException {
        // Il blocco try-with-resources apre la connessione e il preparedStatement
        try (Connection con = ConPool.getConnection(); // Sostituisci ConPool con il tuo gestore del pool
             PreparedStatement ps = con.prepareStatement(INSERT_UTENTE)) {
            
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getPasswordHash());
            ps.setString(5, utente.getRuolo());
            
            ps.executeUpdate();
        }
    }

    @Override
    public Utente doRetrieveByEmail(String email) throws SQLException {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_EMAIL)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // Uso il metodo helper per mappare l'utente
                }
            }
        }
        return null; // Se l'email non esiste, restituisco null
    }

    @Override
    public Utente doRetrieveById(int id) throws SQLException {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Utente> doRetrieveAllClienti() throws SQLException {
        List<Utente> clienti = new ArrayList<>();
        
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_CLIENTI);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                clienti.add(mapRow(rs));
            }
        }
        return clienti;
    }

    /**
     * METODO HELPER (Privato): Trasforma una riga del ResultSet in un oggetto Utente.
     * Evita di duplicare lo stesso identico codice di mappatura in doRetrieveById, 
     */
    
    private Utente mapRow(ResultSet rs) throws SQLException {
        Utente utente = new Utente();
        utente.setId(rs.getInt("id_utente"));
        utente.setNome(rs.getString("nome"));
        utente.getCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPasswordHash(rs.getString("password_hash"));
        utente.setRuolo(rs.getString("ruolo"));
        return utente;
    }
}