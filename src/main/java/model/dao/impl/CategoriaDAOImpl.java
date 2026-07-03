package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.bean.CategoriaBean;
import model.dao.CategoriaDAO;
import utility.ConnessioneDB;

public class CategoriaDAOImpl implements CategoriaDAO {

    private static final String INSERT_CATEGORIA =
        "INSERT INTO categoria (nome, descrizione) VALUES (?, ?)";

    private static final String UPDATE_CATEGORIA =
        "UPDATE categoria SET nome = ?, descrizione = ? WHERE id_categoria = ?";

    private static final String SELECT_BY_ID =
        "SELECT id_categoria, nome, descrizione FROM categoria WHERE id_categoria = ?";

    private static final String SELECT_ALL =
        "SELECT id_categoria, nome, descrizione FROM categoria ORDER BY nome";

    private static final String DELETE_CATEGORIA =
        "DELETE FROM categoria WHERE id_categoria = ?";

    @Override
    public void doSave(CategoriaBean categoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CATEGORIA)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescrizione());

            ps.executeUpdate();
        }
    }

    @Override
    public void doUpdate(CategoriaBean categoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CATEGORIA)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescrizione());
            ps.setInt(3, categoria.getIdCategoria());

            ps.executeUpdate();
        }
    }

    @Override
    public CategoriaBean doRetrieveById(int idCategoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<CategoriaBean> doRetrieveAll() throws SQLException {
        List<CategoriaBean> categorie = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorie.add(mapRow(rs));
            }
        }
        return categorie;
    }

    @Override
    public boolean doDelete(int idCategoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CATEGORIA)) {

            ps.setInt(1, idCategoria);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    private CategoriaBean mapRow(ResultSet rs) throws SQLException {
        CategoriaBean categoria = new CategoriaBean();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNome(rs.getString("nome"));
        categoria.setDescrizione(rs.getString("descrizione"));
        return categoria;
    }
}
