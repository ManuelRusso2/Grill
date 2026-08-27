package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.dao.CategoriaDAO;
import model.dao.ProdottoDAO;
import utility.ConnessioneDB;

/**
 * Implementazione DAO per la gestione della persistenza dei prodotti nel Database.
 * Gestisce le operazioni CRUD, il raggruppamento 
 * delle varianti di prodotto per il catalogo, l'associazione M:N con le categorie 
 * e la cancellazione logica (soft delete).
 */
public class ProdottoDAOImpl implements ProdottoDAO {

    // DAO ausiliario per recuperare le categorie collegate a ciascun prodotto
    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();

    // =========================================================================
    // QUERY SQL PREPARATE
    // =========================================================================

    // Inserisce un nuovo prodotto nel DB
    private static final String INSERT_PRODOTTO =
        "INSERT INTO prodotto (nome, descrizione, costo, iva, quantita, attivo, id_collezione, immagine, taglie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Aggiorna tutti i campi di un prodotto esistente tramite il suo ID
    private static final String UPDATE_PRODOTTO =
        "UPDATE prodotto SET nome = ?, descrizione = ?, costo = ?, iva = ?, quantita = ?, attivo = ?, id_collezione = ?, immagine = ?, taglie = ? WHERE id_prodotto = ?";

    // Soft delete: disattiva il prodotto (attivo = false) per preservare lo storico degli ordini passati
    private static final String DELETE_LOGIC_PRODOTTO =
        "UPDATE prodotto SET attivo = false WHERE id_prodotto = ?";

    // Selezione di un singolo prodotto tramite la sua chiave primaria (ID)
    private static final String SELECT_BY_KEY =
        "SELECT id_prodotto, nome, descrizione, costo, iva, quantita, attivo, immagine, id_collezione, taglie FROM prodotto WHERE id_prodotto = ?";

    // Selezione di tutti i prodotti attivi destinati alla consultazione cliente
    private static final String SELECT_ALL_PRODOTTI =
        "SELECT id_prodotto, nome, descrizione, costo, iva, quantita, attivo, immagine, id_collezione, taglie FROM prodotto WHERE attivo = true";

    // Query vetrina: raggruppa le varianti (es. colore/taglia) sotto un unico nome base e calcola la quantita totale
    private static final String SELECT_ALL_PRODOTTI_RAGGRUPPATI =
        "SELECT MIN(id_prodotto) as id_prodotto, " +
        "SUBSTRING_INDEX(nome, ' - ', 1) as nome, " +
        "MIN(descrizione) as descrizione, MIN(costo) as costo, MIN(iva) as iva, " +
        "SUM(quantita) as quantita, true as attivo, MIN(immagine) as immagine, MIN(id_collezione) as id_collezione, MIN(taglie) as taglie " +
        "FROM prodotto WHERE attivo = true " +
        "GROUP BY SUBSTRING_INDEX(nome, ' - ', 1) " +
        "ORDER BY MIN(id_prodotto)";

    // Selezione di tutte le varianti specifiche legate a un prodotto base
    private static final String SELECT_VARIANTI =
        "SELECT id_prodotto, nome, descrizione, costo, iva, quantita, attivo, immagine, id_collezione, taglie " +
        "FROM prodotto WHERE attivo = true AND SUBSTRING_INDEX(nome, ' - ', 1) = ? " +
        "ORDER BY id_prodotto";

    // Selezione globale di tutti i prodotti (attivi e disattivati) per il pannello di amministrazione
    private static final String SELECT_ALL_ADMIN =
        "SELECT id_prodotto, nome, descrizione, costo, iva, quantita, attivo, immagine, id_collezione, taglie FROM prodotto";

    // Ricerca live per autocompletamento (filtra per nome, descrizione o nome categoria)
    private static final String SELECT_BY_SEARCH =
        "SELECT DISTINCT p.id_prodotto, p.nome, p.costo " +
        "FROM prodotto p LEFT JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "LEFT JOIN categoria c ON t.id_categoria = c.id_categoria " +
        "WHERE p.attivo = true AND (p.nome LIKE ? OR p.descrizione LIKE ? OR c.nome LIKE ?) LIMIT 8";

    // Selezione dei prodotti appartenenti a una specifica categoria
    private static final String SELECT_BY_CATEGORIA =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.iva, p.quantita, p.attivo, p.immagine, p.id_collezione, p.taglie " +
        "FROM prodotto p JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "WHERE p.attivo = true AND t.id_categoria = ?";

    // Rimuove tutte le associazioni del prodotto dalla tabella ponte 'prodotto_categoria'
    private static final String DELETE_TIPOLOGIA =
        "DELETE FROM prodotto_categoria WHERE id_prodotto = ?";

    // Inserisce una nuova associazione nella tabella ponte 'prodotto_categoria'
    private static final String INSERT_TIPOLOGIA =
        "INSERT INTO prodotto_categoria (id_prodotto, id_categoria) VALUES (?, ?)";

    // =========================================================================
    // METODI CRUD PRINCIPALI
    // =========================================================================

    /**
     * Salva un nuovo prodotto nel Database e ne associa le categorie all'interno di una transazione.
     */
    @Override
    public void doSave(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false); // Inizio della transazione SQL
            
            try {
                int idProdotto;
                
                // Preparazione dell'inserimento con recupero della chiave generata (AUTO_INCREMENT)
                try (PreparedStatement ps = con.prepareStatement(INSERT_PRODOTTO, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setDouble(4, prodotto.getIva());
                    ps.setInt(5, prodotto.getQuantita());
                    ps.setBoolean(6, prodotto.isAttivo());
                    setNullableInt(ps, 7, prodotto.getIdCollezione());
                    ps.setString(8, getImmagineOrDefault(prodotto.getImmagine()));
                    ps.setString(9, prodotto.getTaglie());

                    ps.executeUpdate();
                    
                    // Recupera l'ID univoco assegnato al nuovo prodotto
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Impossibile recuperare l'ID autogenerato del nuovo prodotto.");
                        }
                        idProdotto = keys.getInt(1);
                        prodotto.setIdProdotto(idProdotto);
                    }
                }
                
                // Salva le relazioni con le categorie nella tabella ponte
                salvaTipologie(con, idProdotto, prodotto.getCategorie());
                
                con.commit(); // Conferma definitiva della transazione
            } catch (SQLException e) {
                con.rollback(); // Annulla la transazione in caso di errore
                throw e;
            }
        }
    }

    /**
     * Aggiorna i dati di un prodotto esistente nel DB (compresa l'IVA) e sincronizza le sue categorie.
     */
    @Override
    public void doUpdate(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false); // Inizio della transazione SQL
            
            try {
                // Aggiorna le informazioni principali nella tabella 'prodotto'
                try (PreparedStatement ps = con.prepareStatement(UPDATE_PRODOTTO)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setDouble(4, prodotto.getIva());
                    ps.setInt(5, prodotto.getQuantita());
                    ps.setBoolean(6, prodotto.isAttivo());
                    setNullableInt(ps, 7, prodotto.getIdCollezione());
                    ps.setString(8, getImmagineOrDefault(prodotto.getImmagine()));
                    ps.setString(9, prodotto.getTaglie());
                    ps.setInt(10, prodotto.getIdProdotto());

                    ps.executeUpdate();
                }

                // Sincronizzazione categorie: cancella le vecchie associazioni dalla tabella ponte
                try (PreparedStatement ps = con.prepareStatement(DELETE_TIPOLOGIA)) {
                    ps.setInt(1, prodotto.getIdProdotto());
                    ps.executeUpdate();
                }
                
                // Inserisce le nuove associazioni aggiornate
                salvaTipologie(con, prodotto.getIdProdotto(), prodotto.getCategorie());
                
                con.commit(); // Conferma definitiva della transazione
            } catch (SQLException e) {
                con.rollback(); // Annulla la transazione in caso di errore
                throw e;
            }
        }
    }

    /**
     * Disattiva logicamente un prodotto imponendo attivo = false (Soft Delete).
     */
    @Override
    public boolean doDelete(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_LOGIC_PRODOTTO)) {
            ps.setInt(1, idProdotto);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // METODI DI LETTURA E QUERY CATALOGO
    // =========================================================================

    /**
     * Cerca un prodotto nel DB tramite la sua chiave primaria (ID).
     */
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

    /**
     * Recupera tutti i prodotti attualmente attivi sul sito.
     */
    @Override
    public List<ProdottoBean> doRetrieveAllProdotti() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_PRODOTTI);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prodotti.add(mapRow(rs));
            }
        }
        return prodotti;
    }

    /**
     * Recupera l'elenco completo dei prodotti (inclusi quelli disattivati) per l'Admin.
     */
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

    /**
     * Recupera tutti i prodotti associati a una specifica categoria.
     */
    @Override
    public List<ProdottoBean> doRetrieveByCategoria(int idCategoria) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_CATEGORIA)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(mapRow(rs));
                }
            }
        }
        return prodotti;
    }

    /**
     * Recupera la lista dei prodotti raggruppati per nome base da mostrare nel catalogo.
     */
    @Override
    public List<ProdottoBean> doRetrieveAllProdottiRaggruppati() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_PRODOTTI_RAGGRUPPATI);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProdottoBean p = new ProdottoBean();
                p.setIdProdotto(rs.getInt("id_prodotto"));
                p.setNome(rs.getString("nome"));
                p.setDescrizione(rs.getString("descrizione"));
                p.setCosto(rs.getDouble("costo"));
                p.setIva(rs.getDouble("iva")); // Mappa l'IVA
                p.setQuantita(rs.getInt("quantita"));
                p.setAttivo(true);
                p.setImmagine(rs.getString("immagine"));
                p.setIdCollezione(rs.getInt("id_collezione"));
                p.setTaglie(rs.getString("taglie"));
                prodotti.add(p);
            }
        }
        return prodotti;
    }

    /**
     * Recupera le varianti collegate a un nome prodotto base.
     */
    @Override
    public List<ProdottoBean> doRetrieveVarianti(String nomeBase) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_VARIANTI)) {
            ps.setString(1, nomeBase);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(mapRow(rs));
                }
            }
        }
        return prodotti;
    }

    /**
     * Esegue la ricerca rapida per la barra di autocompletamento live.
     */
    @Override
    public List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException {        
        List<ProdottoBean> prodotti = new ArrayList<>();
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

    // =========================================================================
    // METODI AUXILIARI PRIVATI
    // =========================================================================

    /**
     * Salva le relazioni tra il prodotto e le sue categorie tramite query batch.
     */
    private void salvaTipologie(Connection con, int idProdotto, List<CategoriaBean> categorie) throws SQLException {
        if (categorie == null || categorie.isEmpty()) return;
        
        try (PreparedStatement ps = con.prepareStatement(INSERT_TIPOLOGIA)) {
            for (int i = 0; i < categorie.size(); i++) {
                CategoriaBean cat = categorie.get(i);
                ps.setInt(1, idProdotto);
                ps.setInt(2, cat.getIdCategoria());
                ps.addBatch(); // Accoda l'operazione nel batch
            }
            ps.executeBatch(); // Esegue tutti gli inserimenti in una sola chiamata DB
        }
    }

    /**
     * Mappa una riga di ResultSet in un oggetto ProdottoBean.
     */
    private ProdottoBean mapRow(ResultSet rs) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setIdProdotto(rs.getInt("id_prodotto"));
        prodotto.setNome(rs.getString("nome"));
        prodotto.setDescrizione(rs.getString("descrizione"));
        prodotto.setCosto(rs.getDouble("costo"));
        prodotto.setIva(rs.getDouble("iva")); // Legge l'aliquota IVA dal Resultset
        prodotto.setQuantita(rs.getInt("quantita"));
        prodotto.setAttivo(rs.getBoolean("attivo"));
        prodotto.setImmagine(rs.getString("immagine"));
        prodotto.setTaglie(rs.getString("taglie"));
        prodotto.setIdCollezione(rs.getInt("id_collezione"));

        // Recupera ed imposta le categorie associate al prodotto tramite CategoriaDAO
        try {
            prodotto.setCategorie(categoriaDAO.doRetrieveByProdotto(prodotto.getIdProdotto()));
        } catch (SQLException e) {
            prodotto.setCategorie(new ArrayList<>());
        }
        
        return prodotto;
    }

    /**
     * Ritorna l'immagine specificata oppure l'immagine di default se il parametro è vuoto.
     */
    private String getImmagineOrDefault(String immagine) {
        if (immagine != null && !immagine.trim().isEmpty()) {
            return immagine;
        }
        return "images/default.jpg";
    }

    /**
     * Gestisce i campi numerici opzionali (Foreign Keys nullable) inserendo NULL in caso di valore 0 o assente.
     */
    private void setNullableInt(PreparedStatement ps, int paramIndex, Integer value) throws SQLException {
        if (value != null && value > 0) {
            ps.setInt(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.INTEGER);
        }
    }
}