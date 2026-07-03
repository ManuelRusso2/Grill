package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import model.bean.CarrelloBean;
import model.dao.CarrelloDAO;
import utility.ConnessioneDB;

public class CarrelloDAOImpl implements CarrelloDAO {

    private static final String INSERT_CARRELLO =
        "INSERT INTO carrello (id_utente) VALUES (?)";

    
    private static final String SELECT_BY_UTENTE =
        "SELECT id_carrello, id_utente FROM carrello WHERE id_utente = ?";

    
    private static final String SELECT_BY_ID =
        "SELECT id_carrello, id_utente FROM carrello WHERE id_carrello = ?";

    
    private static final String UPDATE_CARRELLO =
        "UPDATE carrello SET id_utente = ? WHERE id_carrello = ?";

    
    private static final String DELETE_CONTENUTO_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ?";

    
    private static final String DELETE_CARRELLO =
        "DELETE FROM carrello WHERE id_carrello = ?";

    
    @Override
    public void doSave(CarrelloBean carrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CARRELLO)) {

            ps.setInt(1, carrello.getIdUtente());
            ps.executeUpdate();
        }
    }

    
    @Override
    public CarrelloBean doRetrieveByUtente(int idUtente) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    
    @Override
    public CarrelloBean doRetrieveById(int idCarrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idCarrello);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    
    @Override
    public void doUpdate(CarrelloBean carrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CARRELLO)) {

            ps.setInt(1, carrello.getIdUtente());
            ps.setInt(2, carrello.getIdCarrello());

            ps.executeUpdate();
        }
    }

    
    @Override
    public boolean doEmpty(int idCarrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CONTENUTO_CARRELLO)) {

            ps.setInt(1, idCarrello);
            int rows = ps.executeUpdate();
            return rows >= 0;
        }
    }

    
    @Override
    public boolean doDelete(int idCarrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CARRELLO)) {

            ps.setInt(1, idCarrello);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    
    private CarrelloBean mapRow(ResultSet rs) throws SQLException {
        CarrelloBean carrello = new CarrelloBean();
        carrello.setIdCarrello(rs.getInt("id_carrello"));
        carrello.setIdUtente(rs.getInt("id_utente"));
        carrello.setProdotti(new HashMap<>());
        return carrello;
    }
}
