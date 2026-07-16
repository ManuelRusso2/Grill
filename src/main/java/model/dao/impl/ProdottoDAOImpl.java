package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import utility.ConnessioneDB;

public class ProdottoDAOImpl implements ProdottoDAO {

    // Query SQL con PreparedStatement per prevenire SQL Injection
    private static final String INSERT_PRODOTTO = 
        "INSERT INTO prodotto (nome, descrizione, costo, quantita, tipo, attivo, id_collezione) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    
    private static final String UPDATE_PRODOTTO = 
        "UPDATE prodotto SET nome = ?, descrizione = ?, costo = ?, quantita = ?, tipo = ?, attivo = ?, id_collezione = ? WHERE id_prodotto = ?";
    
    
    // Cancellazione Logica: cambia il flag attivo a false per preservare l'integrità referenziale negli ordini storici
    private static final String DELETE_LOGIC_PRODOTTO = 
        "UPDATE prodotto SET attivo = false WHERE id_prodotto = ?";
    
    
    private static final String SELECT_BY_KEY = 
        "SELECT id_prodotto, nome, descrizione, costo, quantita, tipo, attivo, id_collezione FROM prodotto WHERE id_prodotto = ?";
    
    
    // Il cliente vede solo i prodotti attivi nel catalogo
    private static final String SELECT_ALL_CLIENTI = 
        "SELECT id_prodotto, nome, descrizione, costo, quantita, tipo, attivo, id_collezione FROM prodotto WHERE attivo = true";
    
    
    // L'admin vede tutto il catalogo, inclusi i prodotti disattivati/archiviati
    private static final String SELECT_ALL_ADMIN = 
        "SELECT id_prodotto, nome, descrizione, costo, quantita, tipo, attivo, id_collezione FROM prodotto";
    
    
    // Query AJAX per i suggerimenti della barra di ricerca (filtra per prodotti attivi)
    private static final String SELECT_BY_SEARCH = 
        "SELECT id_prodotto, nome, descrizione, costo, quantita, tipo, attivo, id_collezione FROM prodotto WHERE attivo = true AND nome LIKE ?";

    
    @Override
    public void doSave(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_PRODOTTO)) {
            
            ps.setString(1, prodotto.getNome());
            ps.setString(2, prodotto.getDescrizione());
            ps.setDouble(3, prodotto.getCosto());
            ps.setInt(4, prodotto.getQuantita());
            ps.setString(5, prodotto.getTipo());
            ps.setBoolean(6, prodotto.isAttivo());
            
            if (prodotto.getIdCollezione() != null && prodotto.getIdCollezione() > 0) {
                ps.setInt(7, prodotto.getIdCollezione());
            } else {
                ps.setNull(7, Types.INTEGER); // Gestisce la foreign key opzionale se non legata a una collezione
            }
            
            ps.executeUpdate();
        }
    }

    
    @Override
    public void doUpdate(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_PRODOTTO)) {
            
            ps.setString(1, prodotto.getNome());
            ps.setString(2, prodotto.getDescrizione());
            ps.setDouble(3, prodotto.getCosto());
            ps.setInt(4, prodotto.getQuantita());
            ps.setString(5, prodotto.getTipo());
            ps.setBoolean(6, prodotto.isAttivo());
            
            if (prodotto.getIdCollezione() != null && prodotto.getIdCollezione() > 0) {
                ps.setInt(7, prodotto.getIdCollezione());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, prodotto.getIdProdotto());
            
            ps.executeUpdate();
        }
    }

    
    @Override
    public boolean doDelete(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_LOGIC_PRODOTTO)) {
            
            ps.setInt(1, idProdotto);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    
    @Override
    public ProdottoBean doRetrieveByKey(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_KEY)) {
            
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    
    @Override
    public List<ProdottoBean> doRetrieveAllClienti() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_CLIENTI);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                prodotti.add(mapRow(rs));
            }
        }
        return prodotti;
    }

    
    @Override
    public List<ProdottoBean> doRetrieveAllAdmin() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_ADMIN);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                prodotti.add(mapRow(rs));
            }
        }
        return prodotti;
    }

    
    @Override
    public List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_SEARCH)) {
            
            // Imposta il parametro per il LIKE (es. se query è "Grill", diventerà "%Grill%"), 
        	// quindi trova tutti i prodotti che contengono la parola 'Grill' in qualsiasi punto del testo
            ps.setString(1, "%" + query + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(mapRow(rs));
                }
            }
        }
        return prodotti;
    }

    
    /**
     * Helper Method per mappare i campi della tabella Prodotto nel rispettivo Bean.
     */
    private ProdottoBean mapRow(ResultSet rs) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setIdProdotto(rs.getInt("id_prodotto"));
        prodotto.setNome(rs.getString("nome"));
        prodotto.setDescrizione(rs.getString("descrizione"));
        prodotto.setCosto(rs.getDouble("costo"));
        prodotto.setQuantita(rs.getInt("quantita"));
        prodotto.setTipo(rs.getString("tipo"));
        prodotto.setAttivo(rs.getBoolean("attivo"));
        prodotto.setIdCollezione(rs.getInt("id_collezione"));
        return prodotto;
    }
}