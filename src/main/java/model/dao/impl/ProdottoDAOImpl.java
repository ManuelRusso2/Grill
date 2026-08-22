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
 * Implementazione dell'interfaccia ProdottoDAO per la gestione della persistenza dei prodotti nel DB.
 * Gestisce la logica CRUD dei prodotti, le varianti di prodotto, il raggruppamento dinamico, 
 * le associazioni con le categorie e la cancellazione logica (soft delete).
 */
public class ProdottoDAOImpl implements ProdottoDAO {

    // DAO per gestire le relazioni con la tabella delle Categorie
    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();

    // --- QUERY SQL PREPARATE ---

    // Inserimento di un nuovo prodotto
    private static final String INSERT_PRODOTTO =
        "INSERT INTO prodotto (nome, descrizione, costo, quantita, attivo, id_collezione, immagine, taglie) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    // Aggiornamento dati di un prodotto
    private static final String UPDATE_PRODOTTO =
        "UPDATE prodotto SET nome = ?, descrizione = ?, costo = ?, quantita = ?, attivo = ?, id_collezione = ?, immagine = ?, taglie = ? WHERE id_prodotto = ?";

    // Soft delete: disattiva il prodotto invece di eliminare la riga (per preservare lo storico ordini)
    private static final String DELETE_LOGIC_PRODOTTO =
        "UPDATE prodotto SET attivo = false WHERE id_prodotto = ?";

    // Selezione per ID univoco
    private static final String SELECT_BY_KEY =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, immagine, id_collezione, taglie FROM prodotto WHERE id_prodotto = ?";

    // Selezione di tutti i prodotti visibili ai clienti (attivo = true)
    private static final String SELECT_ALL_PRODOTTI =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, immagine, id_collezione, taglie FROM prodotto WHERE attivo = true";

    // Query per la vetrina: raggruppa le varianti dello stesso prodotto in un unico elemento dimostrativo
    private static final String SELECT_ALL_PRODOTTI_RAGGRUPPATI =
        "SELECT MIN(id_prodotto) as id_prodotto, " +
        "SUBSTRING_INDEX(nome, ' - ', 1) as nome, " +
        "MIN(descrizione) as descrizione, MIN(costo) as costo, " +
        "SUM(quantita) as quantita, true as attivo, MIN(immagine) as immagine, MIN(id_collezione) as id_collezione, MIN(taglie) as taglie " +
        "FROM prodotto WHERE attivo = true " +
        "GROUP BY SUBSTRING_INDEX(nome, ' - ', 1) " +
        "ORDER BY MIN(id_prodotto)";

    // Selezione delle specifiche varianti associate a un prodotto base (es. taglie/colori)
    private static final String SELECT_VARIANTI =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, immagine, id_collezione, taglie " +
        "FROM prodotto WHERE attivo = true AND SUBSTRING_INDEX(nome, ' - ', 1) = ? " +
        "ORDER BY id_prodotto";

    // Selezione di tutti i prodotti (inclusi quelli disattivati) per il pannello di amministrazione
    private static final String SELECT_ALL_ADMIN =
        "SELECT id_prodotto, nome, descrizione, costo, quantita, attivo, immagine, id_collezione, taglie FROM prodotto";

    // Ricerca live/autocompletamento per nome, descrizione o categoria (limitato a 8 risultati per efficienza)
    private static final String SELECT_BY_SEARCH =
        "SELECT DISTINCT p.id_prodotto, p.nome, p.costo " +
        "FROM prodotto p LEFT JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "LEFT JOIN categoria c ON t.id_categoria = c.id_categoria " +
        "WHERE p.attivo = true AND (p.nome LIKE ? OR p.descrizione LIKE ? OR c.nome LIKE ?) LIMIT 8";

    // Selezione prodotti filtrati per id_categoria
    private static final String SELECT_BY_CATEGORIA =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.quantita, p.attivo, p.immagine, p.id_collezione, p.taglie " +
        "FROM prodotto p JOIN prodotto_categoria t ON p.id_prodotto = t.id_prodotto " +
        "WHERE p.attivo = true AND t.id_categoria = ?";

    // Rimozione delle relazioni con le categorie (tabella ponte M:N)
    private static final String DELETE_TIPOLOGIA =
        "DELETE FROM prodotto_categoria WHERE id_prodotto = ?";

    // Inserimento relazione con le categorie (tabella ponte M:N)
    private static final String INSERT_TIPOLOGIA =
        "INSERT INTO prodotto_categoria (id_prodotto, id_categoria) VALUES (?, ?)";

    /**
     * Inserisce un nuovo prodotto nel DB e ne salva le categorie collegate in una transazione atomica.
     * 
     * @param prodotto L'oggetto ProdottoBean da persistere
     * @throws SQLException Se si verifica un errore durante le operazioni di inserimento
     */
    @Override
    public void doSave(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false); // Avvio transazione
            try {
                int idProdotto;
                try (PreparedStatement ps = con.prepareStatement(INSERT_PRODOTTO, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setInt(4, prodotto.getQuantita());
                    ps.setBoolean(5, prodotto.isAttivo());
                    setNullableInt(ps, 6, prodotto.getIdCollezione());
                    ps.setString(7, getImmagineOrDefault(prodotto.getImmagine()));
                    ps.setString(8, prodotto.getTaglie());

                    ps.executeUpdate();
                    
                    // Recupero della chiave primaria autogenerata
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Errore: Impossibile recuperare l'ID prodotto generato.");
                        }
                        idProdotto = keys.getInt(1);
                        prodotto.setIdProdotto(idProdotto);
                    }
                }
                
                // Salvataggio delle associazioni con le categorie (M:N)
                salvaTipologie(con, idProdotto, prodotto.getCategorie());
                con.commit(); // Conferma transazione
            } catch (SQLException e) {
                con.rollback(); // Annulla transazione in caso di fallimento
                throw e;
            }
        }
    }

    /**
     * Aggiorna un prodotto esistente e ne ri-sincronizza la lista delle categorie collegate.
     * 
     * @param prodotto L'oggetto ProdottoBean aggiornato
     * @throws SQLException Se si verifica un errore durante l'aggiornamento
     */
    @Override
    public void doUpdate(ProdottoBean prodotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            con.setAutoCommit(false); // Avvio transazione
            try {
                try (PreparedStatement ps = con.prepareStatement(UPDATE_PRODOTTO)) {
                    ps.setString(1, prodotto.getNome());
                    ps.setString(2, prodotto.getDescrizione());
                    ps.setDouble(3, prodotto.getCosto());
                    ps.setInt(4, prodotto.getQuantita());
                    ps.setBoolean(5, prodotto.isAttivo());
                    setNullableInt(ps, 6, prodotto.getIdCollezione());
                    ps.setString(7, getImmagineOrDefault(prodotto.getImmagine()));
                    ps.setString(8, prodotto.getTaglie());
                    ps.setInt(9, prodotto.getIdProdotto());

                    ps.executeUpdate();
                }

                // Sincronizzazione categorie: cancella le vecchie e inserisce le nuove
                try (PreparedStatement ps = con.prepareStatement(DELETE_TIPOLOGIA)) {
                    ps.setInt(1, prodotto.getIdProdotto());
                    ps.executeUpdate();
                }
                
                salvaTipologie(con, prodotto.getIdProdotto(), prodotto.getCategorie());
                con.commit(); // Conferma transazione
            } catch (SQLException e) {
                con.rollback(); // Annulla transazione
                throw e;
            }
        }
    }

    /**
     * Esegue una cancellazione LOGICA (soft delete) impostando 'attivo = false'.
     * Mantiene l'integrità dei dati referenziati dagli ordini passati.
     * 
     * @param idProdotto L'ID del prodotto da disattivare
     * @return true se l'operazione ha avuto successo
     * @throws SQLException In caso di errore SQL
     */
    @Override
    public boolean doDelete(int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_LOGIC_PRODOTTO)) {
            ps.setInt(1, idProdotto);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Recupera un singolo prodotto dal database tramite il suo ID.
     * 
     * @param idProdotto L'ID del prodotto
     * @return L'oggetto ProdottoBean associato o null se non trovato
     */
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

    /**
     * Recupera l'elenco di tutti i prodotti attivi
     */
    @Override
    public List<ProdottoBean> doRetrieveAllProdotti() throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_PRODOTTI);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prodotti.add(mapRow(rs));
        }
        return prodotti;
    }

    /**
     * Recupera tutti i prodotti presenti nel sistema (inclusi quelli disattivati).
     * Riservato all'utilizzo del pannello Admin.
     */
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

    /**
     * Recupera la lista di prodotti appartenenti a una specifica categoria.
     * 
     * @param idCategoria L'ID della categoria da filtrare
     */
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

    /**
     * Recupera i prodotti attivi raggruppati per nome base
     * Somma la quantità totale disponibile per tutte le varianti correlate.
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
     * Recupera tutte le varianti specifiche per un dato nome prodotto base.
     * 
     * @param nomeBase Il nome identificativo condiviso tra le varianti
     */
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

    /**
     * Ricerca avanzata/suggerimenti live per il modulo di ricerca del sito.
     * Cerca all'interno di: nome prodotto, descrizione e nome della categoria.
     * 
     * @param query Testo digitato dall'utente
     * @return Una lista ridotta di oggetti ProdottoBean popolati solo con i campi essenziali
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

    /**
     * Metodo privato per l'inserimento delle categorie collegate al prodotto 
     */
    private void salvaTipologie(Connection con, int idProdotto, List<CategoriaBean> categorie) throws SQLException {
        if (categorie == null || categorie.isEmpty()) return;
        try (PreparedStatement ps = con.prepareStatement(INSERT_TIPOLOGIA)) {
            for (CategoriaBean cat : categorie) {
                ps.setInt(1, idProdotto);
                ps.setInt(2, cat.getIdCategoria());
                ps.addBatch(); // Accoda la query nel batch
            }
            ps.executeBatch(); // Esegue le query accodate in un'unica chiamata DB
        }
    }

    /**
     * Mappa una riga di ResultSet in un oggetto ProdottoBean, inclusa l'estrazione
     * differita/annidata delle categorie associate tramite `categoriaDAO`.
     */
    private ProdottoBean mapRow(ResultSet rs) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setIdProdotto(rs.getInt("id_prodotto"));
        prodotto.setNome(rs.getString("nome"));
        prodotto.setDescrizione(rs.getString("descrizione"));
        prodotto.setCosto(rs.getDouble("costo"));
        prodotto.setQuantita(rs.getInt("quantita"));
        prodotto.setAttivo(rs.getBoolean("attivo"));
        prodotto.setImmagine(rs.getString("immagine"));
        prodotto.setTaglie(rs.getString("taglie"));
        prodotto.setIdCollezione(rs.getInt("id_collezione"));

        // Caricamento delle categorie collegate tramite il relativo DAO
        try {
            prodotto.setCategorie(categoriaDAO.doRetrieveByProdotto(prodotto.getIdProdotto()));
        } catch (SQLException e) {
            prodotto.setCategorie(new ArrayList<>());
        }
        return prodotto;
    }

    /**
     * Utility method: Ritorna il percorso dell'immagine fornito o un'immagine di fallback di default.
     */
    private String getImmagineOrDefault(String immagine) {
        return (immagine != null && !immagine.trim().isEmpty()) ? immagine : "images/default.jpg";
    }

    /**
     * Utility method: Gestisce i valori interi opzionali (Foreign Keys nullable) 
     * impostando NULL su PreparedStatement se il valore è null/0.
     */
    private void setNullableInt(PreparedStatement ps, int paramIndex, Integer value) throws SQLException {
        if (value != null && value > 0) {
            ps.setInt(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.INTEGER);
        }
    }
}