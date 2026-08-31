package model.dao.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia UtenteDAO per l'accesso ai dati della tabella 'utente'.
 * Gestisce le operazioni CRUD e le logiche relative all'autenticazione.
 */
public class UtenteDAOImpl implements UtenteDAO {

    // =========================================================================
    // QUERY SQL PREPARATE
    // =========================================================================

    private static final String INSERT_UTENTE =
        "INSERT INTO utente (nome, cognome, email, password, telefono, isAdmin) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_CARRELLO = 
        "INSERT INTO carrello (id_utente) VALUES (?)";

    private static final String SELECT_BY_EMAIL =
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE email = ?";

    private static final String SELECT_BY_ID =
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE id_utente = ?";

    private static final String SELECT_BY_LOGIN =
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE email = ? AND password = ?";

    private static final String SELECT_ALL_CLIENTI =
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE isAdmin = false";

    /**
     * Salva un nuovo utente nel database e gli assegna automaticamente un carrello.
     * Utilizza una transazione ACID per garantire che l'utente non venga creato senza il rispettivo carrello.
     * 
     * @param utente L'oggetto UtenteBean da inserire nel DB
     * @throws SQLException In caso di errore nell'esecuzione delle query
     */
    @Override
    public void doSave(UtenteBean utente) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            // Disabilita l'auto-commit per iniziare una transazione manuale
            con.setAutoCommit(false);

            try (PreparedStatement psUtente = con.prepareStatement(INSERT_UTENTE, Statement.RETURN_GENERATED_KEYS)) {
                // Impostazione dei parametri della query per l'utente
                psUtente.setString(1, utente.getNome());
                psUtente.setString(2, utente.getCognome());
                psUtente.setString(3, utente.getEmail());
                psUtente.setString(4, hashPassword(utente.getPassword())); // La password viene salvata in Hash SHA-256
                psUtente.setString(5, utente.getTelefono());
                psUtente.setBoolean(6, utente.isAdmin());
                psUtente.executeUpdate();

                // Recupero dell'ID autoincrementale generato dal DBMS
                try (ResultSet generatedKeys = psUtente.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Errore: Impossibile recuperare l'ID utente generato.");
                    }
                    int idUtenteGenerato = generatedKeys.getInt(1);
                    utente.setIdUtente(idUtenteGenerato); // Aggiorna il bean con l'ID assegnato dal DB

                    // Inserimento automatico del carrello associato al nuovo utente
                    try (PreparedStatement psCarrello = con.prepareStatement(INSERT_CARRELLO)) {
                        psCarrello.setInt(1, idUtenteGenerato);
                        psCarrello.executeUpdate();
                    }
                }

                // Se tutto è andato a buon fine, conferma la transazione
                con.commit();

            } catch (SQLException e) {
                // Se una qualsiasi operazione fallisce, esegue il rollback per annullare le modifiche
                con.rollback();
                throw e; // Rilancia l'eccezione verso il livello superiore
            } finally {
                // Ripristina l'autocommit di default per la connessione
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Recupera un utente verificando le credenziali di accesso (Email e Password).
     * 
     * @param email    L'indirizzo email dell'utente
     * @param password La password in chiaro inserita dall'utente
     * @return UtenteBean se le credenziali sono valide, null altrimenti
     */
    @Override
    public UtenteBean doRetrieveByLogin(String email, String password) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_LOGIN)) {
            ps.setString(1, email);
            ps.setString(2, hashPassword(password)); // Confronta l'hash della password fornita con quello sul DB

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cerca un utente nel database a partire dal suo indirizzo email.
     * 
     * @param email L'email da cercare
     * @return UtenteBean corrispondente se trovato, null altrimenti
     */
    @Override
    public UtenteBean doRetrieveByEmail(String email) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cerca un utente nel database tramite il suo ID univoco.
     * 
     * @param id L'identificativo unico dell'utente
     * @return UtenteBean corrispondente se trovato, null altrimenti
     */
    @Override
    public UtenteBean doRetrieveById(int id) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
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

    /**
     * Recupera la lista di tutti gli utenti registrati che NON sono amministratori (isAdmin = false).
     * 
     * @return Lista di oggetti UtenteBean contenente i clienti
     */
    @Override
    public List<UtenteBean> doRetrieveAllClienti() throws SQLException {
        List<UtenteBean> clienti = new ArrayList<>();

        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_CLIENTI);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                clienti.add(mapRow(rs));
            }
        }
        return clienti;
    }

    // =========================================================================
    // METODI HELPER PRIVATI
    // =========================================================================

    /**
     * Metodo helper di supporto per mappare la riga corrente di un ResultSet
     * in un oggetto della classe UtenteBean.
     * 
     * @param rs Il ResultSet posizionato sulla riga da leggere
     * @return L'oggetto UtenteBean popolato
     */
    private UtenteBean mapRow(ResultSet rs) throws SQLException {
        UtenteBean utente = new UtenteBean();
        utente.setIdUtente(rs.getInt("id_utente"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPassword(rs.getString("password"));
        utente.setTelefono(rs.getString("telefono"));
        utente.setAdmin(rs.getBoolean("isAdmin"));
        return utente;
    }

    /**
     * Algoritmo di hashing per la protezione della password.
     * Converte la stringa della password in un valore hash SHA-256 codificato in esadecimale.
     * 
     * @param password La password in chiaro da cifrare
     * @return La stringa contenente l'hash SHA-256 in formato esadecimale
     */
    private String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Aggiunge lo zero iniziale per byte a cifra singola
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante la cifratura della password", e);
        }
    }
}