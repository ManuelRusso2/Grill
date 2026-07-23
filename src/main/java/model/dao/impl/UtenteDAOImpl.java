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

public class UtenteDAOImpl implements UtenteDAO {

    private static final String INSERT_UTENTE = 
        "INSERT INTO utente (nome, cognome, email, password, username, telefono, isAdmin) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE email = ?";

    private static final String SELECT_BY_ID = 
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE id_utente = ?";
    
    private static final String SELECT_BY_LOGIN = 
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE email = ? AND password = ?";
    
    private static final String SELECT_ALL_CLIENTI = 
        "SELECT id_utente, nome, cognome, email, password, telefono, isAdmin FROM utente WHERE isAdmin = false";

    @Override
    public void doSave(UtenteBean utente) throws SQLException {
        String insertCarrello = "INSERT INTO carrello (id_utente) VALUES (?)";
        
        Connection con = null;
        PreparedStatement psUtente = null;
        PreparedStatement psCarrello = null;
        ResultSet generatedKeys = null;

        try {
            con = ConnessioneDB.getConnection();
            
            // 1. Disattiviamo l'auto-commit per gestire la transazione manualmente
            con.setAutoCommit(false);

            // 2. Prepariamo la query chiedendo a MySQL di restituire la chiave primaria generata
            psUtente = con.prepareStatement(INSERT_UTENTE, Statement.RETURN_GENERATED_KEYS);
            
            psUtente.setString(1, utente.getNome());
            psUtente.setString(2, utente.getCognome());
            psUtente.setString(3, utente.getEmail());
            
            String passwordCifrata = hashPassword(utente.getPassword());
            psUtente.setString(4, passwordCifrata);

            // Generiamo un username automatico (dato che il campo non è più richiesto in fase di registrazione)
            String baseUsername = "user";
            if (utente.getEmail() != null && utente.getEmail().contains("@")) {
                baseUsername = utente.getEmail().split("@")[0].replaceAll("[^A-Za-z0-9_]", "_");
                if (baseUsername.isEmpty()) baseUsername = "user";
                if (baseUsername.length() > 24) baseUsername = baseUsername.substring(0, 24);
            }
            String usernameGenerated = baseUsername + "_" + (int)(Math.random() * 9000 + 1000);
            psUtente.setString(5, usernameGenerated);
            psUtente.setString(6, utente.getTelefono());
            psUtente.setBoolean(7, utente.isAdmin());
            
            psUtente.executeUpdate();
            
            // 3. Recuperiamo l'ID appena generato da MySQL
            generatedKeys = psUtente.getGeneratedKeys();
            int idUtenteGenerato = -1;
            if (generatedKeys.next()) {
                idUtenteGenerato = generatedKeys.getInt(1);
                utente.setIdUtente(idUtenteGenerato); // Aggiorniamo il Bean
            } else {
                throw new SQLException("Errore: Impossibile recuperare l'ID utente generato.");
            }

            // 4. Creiamo il carrello associato a quel preciso ID utente
            psCarrello = con.prepareStatement(insertCarrello);
            psCarrello.setInt(1, idUtenteGenerato);
            psCarrello.executeUpdate();
            
            // 5. Se tutto è andato a buon fine, salviamo definitivamente sul DB
            con.commit();
            
        } catch (SQLException e) {
            // Se qualcosa fallisce, annulliamo ogni modifica per evitare dati orfani
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Rilanciamo l'eccezione alla Servlet
        } finally {
            // Chiudiamo tutte le risorse esplicitamente nel blocco finally
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (psCarrello != null) try { psCarrello.close(); } catch (SQLException e) {}
            if (psUtente != null) try { psUtente.close(); } catch (SQLException e) {}
            if (con != null) try { con.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public UtenteBean doRetrieveByLogin(String email, String password) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_LOGIN)) {

            ps.setString(1, email);
            ps.setString(2, hashPassword(password));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

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

    private String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante la cifratura della password", e);
        }
    }
}