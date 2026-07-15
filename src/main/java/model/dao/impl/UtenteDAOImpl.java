package model.dao.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import utility.ConnessioneDB; 

public class UtenteDAOImpl implements UtenteDAO {

    private static final String INSERT_UTENTE = 
        "INSERT INTO utente (nome, cognome, email, password, username, telefono, isAdmin) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE email = ?";


    private static final String SELECT_BY_USERNAME = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE username = ?";
    
    
    private static final String SELECT_BY_ID = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE id_utente = ?";
    
    
    private static final String SELECT_BY_LOGIN = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE email = ? AND password = ?";
    
    
    private static final String SELECT_ALL_CLIENTI = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE isAdmin = false";

    
    @Override
    public void doSave(UtenteBean utente) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_UTENTE)) {
            
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            
            // REQUISITO: Cifriamo la password prima di salvarla nel DB
            String passwordCifrata = hashPassword(utente.getPassword());
            ps.setString(4, passwordCifrata);
            
            ps.setString(5, utente.getUsername());
            ps.setString(6, utente.getTelefono());
            ps.setBoolean(7, utente.isAdmin());
            
            ps.executeUpdate();
        }
    }

    
    /**
     * Recupera un utente verificando email e password.
     * REQUISITO OBBLIGATORIO: Gestione sessione e cifratura.
     */
    @Override
    public UtenteBean doRetrieveByLogin(String email, String password) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_LOGIN)) {
            
            ps.setString(1, email);
            // Cifriamo la password inserita dall'utente nel form per confrontarla con quella nel DB
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
    public UtenteBean doRetrieveByUsername(String username) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_USERNAME)) {

            ps.setString(1, username);

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
        utente.setUsername(rs.getString("username"));
        utente.setTelefono(rs.getString("telefono"));
        utente.setAdmin(rs.getBoolean("isAdmin"));
        return utente;
    }

    
    /**
     * METODO HELPER (Privato): Cifra una stringa utilizzando l'algoritmo SHA-256.
     * Garantisce l'adempimento del vincolo di sicurezza sulle password.
     */
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
            // Se l'algoritmo non viene trovato, rilanciamo un'eccezione a runtime
            throw new RuntimeException("Errore durante la cifratura della password", e);
        }
    }
}