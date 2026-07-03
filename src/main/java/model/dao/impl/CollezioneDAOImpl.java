package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.bean.CollezioneBean;
import model.dao.CollezioneDAO;
import utility.ConnessioneDB;

public class CollezioneDAOImpl implements CollezioneDAO {

    private static final String INSERT_COLLEZIONE =
        "INSERT INTO collezione (nome_collezione, descrizione) VALUES (?, ?)";

    
    private static final String UPDATE_COLLEZIONE =
        "UPDATE collezione SET nome_collezione = ?, descrizione = ? WHERE id_collezione = ?";

    
    private static final String SELECT_BY_ID =
        "SELECT id_collezione, nome_collezione, descrizione, data_creazione FROM collezione WHERE id_collezione = ?";

    
    private static final String SELECT_ALL =
        "SELECT id_collezione, nome_collezione, descrizione, data_creazione FROM collezione ORDER BY nome_collezione";

    
    private static final String DELETE_COLLEZIONE =
        "DELETE FROM collezione WHERE id_collezione = ?";

    
    @Override
    public void doSave(CollezioneBean collezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_COLLEZIONE)) {

            ps.setString(1, collezione.getNomeCollezione());
            ps.setString(2, collezione.getDescrizione());

            ps.executeUpdate();
        }
    }

    
    @Override
    public void doUpdate(CollezioneBean collezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_COLLEZIONE)) {

            ps.setString(1, collezione.getNomeCollezione());
            ps.setString(2, collezione.getDescrizione());
            ps.setInt(3, collezione.getIdCollezione());

            ps.executeUpdate();
        }
    }

    
    @Override
    public CollezioneBean doRetrieveById(int idCollezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idCollezione);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    
    @Override
    public List<CollezioneBean> doRetrieveAll() throws SQLException {
        List<CollezioneBean> collezioni = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                collezioni.add(mapRow(rs));
            }
        }
        return collezioni;
    }

    @Override
    public boolean doDelete(int idCollezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_COLLEZIONE)) {

            ps.setInt(1, idCollezione);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    
    private CollezioneBean mapRow(ResultSet rs) throws SQLException {
        CollezioneBean collezione = new CollezioneBean();
        collezione.setIdCollezione(rs.getInt("id_collezione"));
        collezione.setNomeCollezione(rs.getString("nome_collezione"));
        collezione.setDescrizione(rs.getString("descrizione"));
        collezione.setDataCreazione(rs.getTimestamp("data_creazione"));
        return collezione;
    }
}