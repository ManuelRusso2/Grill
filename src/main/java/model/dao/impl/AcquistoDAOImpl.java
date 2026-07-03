package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.bean.AcquistoBean;
import model.dao.AcquistoDAO;
import utility.ConnessioneDB;

public class AcquistoDAOImpl implements AcquistoDAO {

    private static final String INSERT_ACQUISTO =
        "INSERT INTO acquisto (prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_ACQUISTO =
        "UPDATE acquisto SET prezzo_totale = ?, data_acquisto = ?, metodo_pagamento = ?, indirizzo_consegna = ? WHERE id_acquisto = ?";

    private static final String SELECT_BY_ID =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_acquisto = ?";

    private static final String SELECT_BY_UTENTE =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_utente = ? ORDER BY data_acquisto DESC";

    private static final String SELECT_ALL =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto ORDER BY data_acquisto DESC";

    private static final String DELETE_ACQUISTO =
        "DELETE FROM acquisto WHERE id_acquisto = ?";

    @Override
    public void doSave(AcquistoBean acquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO)) {

            ps.setDouble(1, acquisto.getPrezzoTotale());
            ps.setTimestamp(2, acquisto.getDataAcquisto());
            ps.setString(3, acquisto.getMetodoPagamento());
            ps.setString(4, acquisto.getIndirizzoConsegna());
            ps.setInt(5, acquisto.getIdUtente());

            ps.executeUpdate();
        }
    }

    @Override
    public void doUpdate(AcquistoBean acquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_ACQUISTO)) {

            ps.setDouble(1, acquisto.getPrezzoTotale());
            ps.setTimestamp(2, acquisto.getDataAcquisto());
            ps.setString(3, acquisto.getMetodoPagamento());
            ps.setString(4, acquisto.getIndirizzoConsegna());
            ps.setInt(5, acquisto.getIdAcquisto());

            ps.executeUpdate();
        }
    }

    @Override
    public AcquistoBean doRetrieveById(int idAcquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idAcquisto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<AcquistoBean> doRetrieveByUtente(int idUtente) throws SQLException {
        List<AcquistoBean> acquisti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    acquisti.add(mapRow(rs));
                }
            }
        }
        return acquisti;
    }

    @Override
    public List<AcquistoBean> doRetrieveAll() throws SQLException {
        List<AcquistoBean> acquisti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                acquisti.add(mapRow(rs));
            }
        }
        return acquisti;
    }

    @Override
    public boolean doDelete(int idAcquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_ACQUISTO)) {

            ps.setInt(1, idAcquisto);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    private AcquistoBean mapRow(ResultSet rs) throws SQLException {
        AcquistoBean acquisto = new AcquistoBean();
        acquisto.setIdAcquisto(rs.getInt("id_acquisto"));
        acquisto.setPrezzoTotale(rs.getDouble("prezzo_totale"));
        acquisto.setDataAcquisto(rs.getTimestamp("data_acquisto"));
        acquisto.setMetodoPagamento(rs.getString("metodo_pagamento"));
        acquisto.setIndirizzoConsegna(rs.getString("indirizzo_consegna"));
        acquisto.setIdUtente(rs.getInt("id_utente"));
        return acquisto;
    }
}
