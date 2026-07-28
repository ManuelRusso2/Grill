package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.dao.CategoriaDAO;
import model.dao.ProdottoDAO;
import utility.ConnessioneDB;

public class ProdottoDAOImpl implements ProdottoDAO {

    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();

    private static final String INSERT_PRODOTTO =
        "INSERT INTO prodotto (nome, descrizione, costo, quantita, attivo, id_collezione) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_PRODOTTO =
        "UPDATE prodotto SET nome = ?, descrizione = ?, costo = ?, quantita = ?, attivo = ?, id_collezione = ? WHERE id_prodotto = ?";

    private static final String DELETE_LOGIC_PRODOTTO =
        "UPDATE prodotto SET attivo = false WHERE id_prodotto = ?";

    private static final String SELECT_BY_KEY =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, id_collezione FROM prodotto WHERE id_prodotto = ?";

    private static final String SELECT_ALL_CLIENTI =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, id_collezione FROM prodotto WHERE attivo = true";

    // Restituisce un solo prodotto per ogni nome base (parte prima di ' - '), il primo per id
    private static final String SELECT_ALL_CLIENTI_RAGGRUPPATI =
        "SELECT MIN(id_prodotto) as id_prodotto, " +
        "SUBSTRING_INDEX(nome, ' - ', 1) as nome, " +
        "MIN(descrizione) as descrizione, MIN(costo) as costo, " +
        "SUM(quantita) as quantita, true as attivo, MIN(id_collezione) as id_collezione " +
        "FROM prodotto WHERE attivo = true " +
        "GROUP BY SUBSTRING_INDEX(nome, ' - ', 1) " +
        "ORDER BY MIN(id_prodotto)";

    private static final String SELECT_VARIANTI =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, id_collezione " +
        "FROM prodotto WHERE attivo = true AND SUBSTRING_INDEX(nome, ' - ', 1) = ? " +
        "ORDER BY id_prodotto";

    private static final String SELECT_ALL_ADMIN =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, id_collezione FROM prodotto";

    private static final String SELECT_BY_SEARCH =
        "SELECT DISTINCT p.id_prodotto, p.nome, p.costo " +
        "FROM prodotto p LEFT JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "LEFT JOIN categoria c ON t.id_categoria = c.id_categoria " +
        "WHERE p.attivo = true AND (p.nome LIKE ? OR p.descrizione LIKE ? OR c.nome LIKE ?) LIMIT 8";

    private static final String SELECT_BY_CATEGORIA =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.quantita, p.attivo, p.id_collezione " +
        "FROM prodotto p JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "WHERE p.attivo = true AND t.id_categoria = ?";

    private static final String DELETE_TIPOLOGIA =
        "DELETE FROM prodotto_categoria WHERE id_prodotto = ?";

    private static final String INSERT_TIPOLOGIA =
        "INSERT INTO prodotto_categoria (id_prodotto, id_categoria) VALUES (?, ?)";

    @Override
    public void doSave(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int idProdotto;
                try (PreparedStatement ps = con.prepareStatement(INSERT_PRODOTTO, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setInt(4, prodotto.getQuantita());
                    ps.setBoolean(5, prodotto.isAttivo());
                    if (prodotto.getIdCollezione() != null && prodotto.getIdCollezione() > 0) {
                        ps.setInt(6, prodotto.getIdCollezione());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        idProdotto = keys.getInt(1);
                        prodotto.setIdProdotto(idProdotto);
                    }
                }
                salvaTipologie(con, idProdotto, prodotto.getCategorie());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void doUpdate(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(UPDATE_PRODOTTO)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setInt(4, prodotto.getQuantita());
                    ps.setBoolean(5, prodotto.isAttivo());
                    if (prodotto.getIdCollezione() != null && prodotto.getIdCollezione() > 0) {
                        ps.setInt(6, prodotto.getIdCollezione());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }
                    ps.setInt(7, prodotto.getIdProdotto());
                    ps.executeUpdate();
                }
                // Riscrive le categorie del prodotto
                try (PreparedStatement ps = con.prepareStatement(DELETE_TIPOLOGIA)) {
                    ps.setInt(1, prodotto.getIdProdotto());
                    ps.executeUpdate();
                }
                salvaTipologie(con, prodotto.getIdProdotto(), prodotto.getCategorie());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean doDelete(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_LOGIC_PRODOTTO)) {
            ps.setInt(1, idProdotto);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ProdottoBean doRetrieveByKey(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_KEY)) {
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
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
            while (rs.next()) prodotti.add(mapRow(rs));
        }
        return prodotti;
    }

    @Override
    public List<ProdottoBean> doRetrieveAllAdmin() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_ADMIN);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prodotti.add(mapRow(rs));
        }
        return prodotti;
    }

    @Override
    public List<ProdottoBean> doRetrieveByCategoria(int idCategoria) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_CATEGORIA)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) prodotti.add(mapRow(rs));
            }
        }
        return prodotti;
    }

    @Override
    public List<ProdottoBean> doRetrieveAllClientiRaggruppati() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_CLIENTI_RAGGRUPPATI);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProdottoBean p = new ProdottoBean();
                p.setIdProdotto(rs.getInt("id_prodotto"));
                p.setNome(rs.getString("nome"));
                p.setDescrizione(rs.getString("descrizione"));
                p.setCosto(rs.getDouble("costo"));
                p.setQuantita(rs.getInt("quantita"));
                p.setAttivo(true);
                p.setIdCollezione(rs.getInt("id_collezione"));
                prodotti.add(p);
            }
        }
        return prodotti;
    }

    @Override
    public List<ProdottoBean> doRetrieveVarianti(String nomeBase) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_VARIANTI)) {
            ps.setString(1, nomeBase);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) prodotti.add(mapRow(rs));
            }
        }
        return prodotti;
    }

    @Override
    public List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException {        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_SEARCH)) {
            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProdottoBean p = new ProdottoBean();
                    p.setIdProdotto(rs.getInt("id_prodotto"));
                    p.setNome(rs.getString("nome"));
                    p.setCosto(rs.getDouble("costo"));
                    prodotti.add(p);
                }
            }
        }
        return prodotti;
    }

    private void salvaTipologie(Connection con, int idProdotto, List<CategoriaBean> categorie) throws SQLException {
        if (categorie == null || categorie.isEmpty()) return;
        try (PreparedStatement ps = con.prepareStatement(INSERT_TIPOLOGIA)) {
            for (CategoriaBean cat : categorie) {
                ps.setInt(1, idProdotto);
                ps.setInt(2, cat.getIdCategoria());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private ProdottoBean mapRow(ResultSet rs) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setIdProdotto(rs.getInt("id_prodotto"));
        prodotto.setNome(rs.getString("nome"));
        prodotto.setDescrizione(rs.getString("descrizione"));
        prodotto.setCosto(rs.getDouble("costo"));
        prodotto.setQuantita(rs.getInt("quantita"));
        prodotto.setAttivo(rs.getBoolean("attivo"));
        prodotto.setIdCollezione(rs.getInt("id_collezione"));
        try {
            prodotto.setCategorie(categoriaDAO.doRetrieveByProdotto(prodotto.getIdProdotto()));
        } catch (SQLException e) {
            prodotto.setCategorie(new ArrayList<>());
        }
        return prodotto;
    }
}
