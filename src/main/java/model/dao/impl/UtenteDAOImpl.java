package model.dao.impl;

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
    
    private static final String SELECT_BY_ID = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE id_utente = ?";
    
    // Filtra escludendo gli admin
    private static final String SELECT_ALL_CLIENTI = 
        "SELECT id_utente, nome, cognome, email, password, username, telefono, isAdmin FROM utente WHERE isAdmin = false";

    @Override
    public void doSave(UtenteBean utente) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_UTENTE)) {
            
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getPassword());
            ps.setString(5, utente.getUsername());
            ps.setString(6, utente.getTelefono());
            ps.setBoolean(7, utente.isAdmin());
            
            ps.executeUpdate();
        }
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

    
    /**
     * METODO HELPER (Privato): Trasforma una riga del ResultSet in un oggetto UtenteBean.
     */
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
}