package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.bean.RecensioneBean;
import model.dao.RecensioneDAO;
import utility.ConnessioneDB;

public class RecensioneDAOImpl implements RecensioneDAO {

    private static final String INSERT_RECENSIONE =
        "INSERT INTO recensione (descrizione, valutazione, id_prodotto, id_utente) VALUES (?, ?, ?, ?)";

    
    private static final String UPDATE_RECENSIONE =
        "UPDATE recensione SET descrizione = ?, valutazione = ? WHERE id_recensione = ?";

    
    private static final String SELECT_BY_ID =
        "SELECT id_recensione, data_recensione, descrizione, valutazione, id_prodotto, id_utente FROM recensione WHERE id_recensione = ?";

    
    private static final String SELECT_BY_PRODOTTO =
        "SELECT id_recensione, data_recensione, descrizione, valutazione, id_prodotto, id_utente FROM recensione WHERE id_prodotto = ? ORDER BY data_recensione DESC";

    
    private static final String SELECT_BY_UTENTE =
        "SELECT id_recensione, data_recensione, descrizione, valutazione, id_prodotto, id_utente FROM recensione WHERE id_utente = ? ORDER BY data_recensione DESC";

    
    private static final String SELECT_ALL =
        "SELECT id_recensione, data_recensione, descrizione, valutazione, id_prodotto, id_utente FROM recensione ORDER BY data_recensione DESC";

    
    private static final String DELETE_RECENSIONE =
        "DELETE FROM recensione WHERE id_recensione = ?";

    
    @Override
    public void doSave(RecensioneBean recensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_RECENSIONE)) {

            ps.setString(1, recensione.getDescrizione());
            ps.setDouble(2, recensione.getValutazione());
            ps.setInt(3, recensione.getIdProdotto());
            ps.setInt(4, recensione.getIdUtente());

            ps.executeUpdate();
        }
    }

    
    @Override
    public void doUpdate(RecensioneBean recensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_RECENSIONE)) {

            ps.setString(1, recensione.getDescrizione());
            ps.setDouble(2, recensione.getValutazione());
            ps.setInt(3, recensione.getIdRecensione());

            ps.executeUpdate();
        }
    }

    
    @Override
    public RecensioneBean doRetrieveById(int idRecensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idRecensione);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    
    @Override
    public List<RecensioneBean> doRetrieveByProdotto(int idProdotto) throws SQLException {
        List<RecensioneBean> recensioni = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_PRODOTTO)) {

            ps.setInt(1, idProdotto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recensioni.add(mapRow(rs));
                }
            }
        }
        return recensioni;
    }

    
    @Override
    public List<RecensioneBean> doRetrieveByUtente(int idUtente) throws SQLException {
        List<RecensioneBean> recensioni = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recensioni.add(mapRow(rs));
                }
            }
        }
        return recensioni;
    }

    
    @Override
    public List<RecensioneBean> doRetrieveAll() throws SQLException {
        List<RecensioneBean> recensioni = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                recensioni.add(mapRow(rs));
            }
        }
        return recensioni;
    }

    
    @Override
    public boolean doDelete(int idRecensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_RECENSIONE)) {

            ps.setInt(1, idRecensione);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    
    private RecensioneBean mapRow(ResultSet rs) throws SQLException {
        RecensioneBean recensione = new RecensioneBean();
        recensione.setIdRecensione(rs.getInt("id_recensione"));
        recensione.setDataRecensione(rs.getTimestamp("data_recensione"));
        recensione.setDescrizione(rs.getString("descrizione"));
        recensione.setValutazione(rs.getDouble("valutazione"));
        recensione.setIdProdotto(rs.getInt("id_prodotto"));
        recensione.setIdUtente(rs.getInt("id_utente"));
        return recensione;
    }
}