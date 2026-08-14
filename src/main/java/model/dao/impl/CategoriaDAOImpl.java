package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.bean.CategoriaBean;
import model.dao.CategoriaDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia CategoriaDAO per la gestione delle categorie dei prodotti.
 * Fornisce le operazioni CRUD (Create, Read, Update, Delete) e il recupero delle categorie
 * associate a uno specifico prodotto tramite tabella di giunzione.
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    // --- QUERY SQL PREPARATE ---

    // Inserimento di una nuova categoria (id_categoria è generato automaticamente)
    private static final String INSERT_CATEGORIA =
        "INSERT INTO categoria (nome, descrizione) VALUES (?, ?)";

    // Aggiornamento dei dettagli di una categoria esistente
    private static final String UPDATE_CATEGORIA =
        "UPDATE categoria SET nome = ?, descrizione = ? WHERE id_categoria = ?";

    // Selezione di una singola categoria tramite ID
    private static final String SELECT_BY_ID =
        "SELECT id_categoria, nome, descrizione FROM categoria WHERE id_categoria = ?";

    // Selezione di tutte le categorie ordinate alfabeticamente per nome
    private static final String SELECT_ALL =
        "SELECT id_categoria, nome, descrizione FROM categoria ORDER BY nome";

    // Selezione delle categorie associate a un determinato prodotto (JOIN con tabella ponte prodotto_categoria)
    private static final String SELECT_BY_PRODOTTO =
        "SELECT c.id_categoria, c.nome, c.descrizione FROM categoria c " +
        "JOIN prodotto_categoria t ON c.id_categoria = t.id_categoria WHERE t.id_prodotto = ?";

    // Eliminazione di una categoria dal database
    private static final String DELETE_CATEGORIA =
        "DELETE FROM categoria WHERE id_categoria = ?";

    /**
     * Salva una nuova categoria nel database e recupera l'ID autogenerato.
     * 
     * @param categoria L'oggetto CategoriaBean da persistere
     * @throws SQLException In caso di errore durante l'inserimento
     */
    @Override
    public void doSave(CategoriaBean categoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CATEGORIA, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescrizione());

            ps.executeUpdate();

            // Recupero dell'ID autogenerato dal database
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    categoria.setIdCategoria(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Aggiorna nome e descrizione di una categoria esistente.
     * 
     * @param categoria L'oggetto CategoriaBean contenente i dati aggiornati
     * @throws SQLException In caso di errore durante l'aggiornamento
     */
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

    /**
     * Cerca una categoria in base al suo identificativo univoco.
     * 
     * @param idCategoria L'ID della categoria da recuperare
     * @return L'oggetto CategoriaBean corrispondente, oppure null se non trovata
     * @throws SQLException In caso di errore durante la query
     */
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

    /**
     * Recupera l'elenco completo di tutte le categorie presenti, ordinate alfabeticamente.
     * 
     * @return Una lista di oggetti CategoriaBean
     * @throws SQLException In caso di errore durante la query
     */
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

    /**
     * Recupera tutte le categorie assegnate a uno specifico prodotto 
     * interrogando la tabella di giunzione `prodotto_categoria`.
     * 
     * @param idProdotto L'ID del prodotto di cui si vogliono conoscere le categorie
     * @return Lista delle categorie associate al prodotto
     * @throws SQLException In caso di errore durante la query con JOIN
     */
    @Override
    public List<CategoriaBean> doRetrieveByProdotto(int idProdotto) throws SQLException {
        List<CategoriaBean> categorie = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_PRODOTTO)) {

            ps.setInt(1, idProdotto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorie.add(mapRow(rs));
                }
            }
        }
        return categorie;
    }

    /**
     * Rimuove una categoria dal database in base al suo ID.
     * 
     * @param idCategoria L'ID della categoria da eliminare
     * @return true se la riga è stata eliminata con successo, false altrimenti
     * @throws SQLException In caso di errore durante l'eliminazione
     */
    @Override
    public boolean doDelete(int idCategoria) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CATEGORIA)) {

            ps.setInt(1, idCategoria);
            return ps.executeUpdate() > 0; // Restituisce true se l'eliminazione ha modificato almeno una riga
        }
    }

    /**
     * Metodo helper privato che mappa la riga corrente del ResultSet
     * in un oggetto CategoriaBean.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto CategoriaBean popolato
     * @throws SQLException In caso di errore di lettura dei dati
     */
    private CategoriaBean mapRow(ResultSet rs) throws SQLException {
        CategoriaBean categoria = new CategoriaBean();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNome(rs.getString("nome"));
        categoria.setDescrizione(rs.getString("descrizione"));
        return categoria;
    }
}