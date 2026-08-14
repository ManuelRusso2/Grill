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

/**
 * Implementazione dell'interfaccia RecensioneDAO per la gestione della persistenza 
 * delle recensioni dei prodotti nel database.
 */
public class RecensioneDAOImpl implements RecensioneDAO {

    // --- QUERY SQL PREPARATE ---
    
    // Inserimento di una nuova recensione
    private static final String INSERT_RECENSIONE =
        "INSERT INTO recensione (descrizione, valutazione, id_prodotto, id_utente) VALUES (?, ?, ?, ?)";

    // Aggiornamento del testo e del voto di una recensione esistente
    private static final String UPDATE_RECENSIONE =
        "UPDATE recensione SET descrizione = ?, valutazione = ? WHERE id_recensione = ?";

    // Query di base in JOIN con 'utente' e 'prodotto' per arricchire i dati della recensione 
    // con nome/cognome/email dell'autore e il nome del prodotto recensito.
    private static final String SELECT_BASE =
        "SELECT r.id_recensione, r.data_recensione, r.descrizione, r.valutazione, r.id_prodotto, r.id_utente, " +
        "u.nome, u.cognome, u.email, p.nome AS nome_prodotto " +
        "FROM recensione r " +
        "LEFT JOIN utente u ON r.id_utente = u.id_utente " +
        "LEFT JOIN prodotto p ON r.id_prodotto = p.id_prodotto ";

    // Varianti della query SELECT con filtri e ordinamenti specifici
    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE r.id_recensione = ?";
    private static final String SELECT_BY_PRODOTTO = SELECT_BASE + "WHERE r.id_prodotto = ? ORDER BY r.data_recensione DESC";
    private static final String SELECT_BY_UTENTE = SELECT_BASE + "WHERE r.id_utente = ? ORDER BY r.data_recensione DESC";
    private static final String SELECT_ALL = SELECT_BASE + "ORDER BY r.data_recensione DESC";

    // Eliminazione di una recensione tramite ID
    private static final String DELETE_RECENSIONE =
        "DELETE FROM recensione WHERE id_recensione = ?";

    /**
     * Salva una nuova recensione all'interno del database.
     * La data della recensione viene tipicamente gestita in automatico dal DBMS
     * 
     * @param recensione L'oggetto RecensioneBean contenente i dati da inserire
     * @throws SQLException In caso di errori durante l'esecuzione della query SQL
     */
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

    /**
     * Aggiorna il testo descrittivo e il punteggio di valutazione di una recensione già esistente.
     * 
     * @param recensione L'oggetto RecensioneBean con le informazioni aggiornate e l'ID riferimento
     * @throws SQLException In caso di errore durante l'aggiornamento sul DB
     */
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

    /**
     * Recupera una recensione specifica tramite il suo ID univoco.
     * 
     * @param idRecensione L'identificativo unico della recensione
     * @return L'oggetto RecensioneBean completo di dati utente e prodotto, oppure null se non trovata
     * @throws SQLException In caso di errore nella query di lettura
     */
    @Override
    public RecensioneBean doRetrieveById(int idRecensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            
            ps.setInt(1, idRecensione);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Recupera l'elenco di tutte le recensioni associate ad un determinato prodotto,
     * ordinate dalla più recente alla più meno recente.
     * 
     * @param idProdotto L'ID del prodotto di cui si vogliono leggere le recensioni
     * @return Lista di oggetti RecensioneBean associati al prodotto
     * @throws SQLException In caso di errori nell'estrazione dati dal DB
     */
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

    /**
     * Recupera tutte le recensioni scritte da uno specifico utente,
     * ordinate in modo decrescente per data di pubblicazione.
     * 
     * @param idUtente L'ID dell'utente autore delle recensioni
     * @return Lista di oggetti RecensioneBean scritti dall'utente
     * @throws SQLException In caso di errore durante la query
     */
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

    /**
     * Recupera lo storico completo di tutte le recensioni presenti nel sistema, 
     * ordinate per data di inserimento decrescente (dalla più recente).
     * 
     * @return Lista di tutte le recensioni presenti nel database
     * @throws SQLException In caso di errore durante l'estrazione
     */
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

    /**
     * Elimina una recensione dal database identificandola tramite il suo ID.
     * 
     * @param idRecensione L'ID della recensione da cancellare
     * @return true se l'eliminazione ha rimosso la riga con successo, false altrimenti
     * @throws SQLException In caso di errore durante l'operazione di DELETE
     */
    @Override
    public boolean doDelete(int idRecensione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_RECENSIONE)) {
            
            ps.setInt(1, idRecensione);
            return ps.executeUpdate() > 0; // Restituisce true se almeno una riga è stata cancellata
        }
    }

    /**
     * Metodo helper privato che esegue il mapping
     * da una riga di ResultSet a un oggetto RecensioneBean.
     * 
     * Estrae sia le proprietà della tabella 'recensione', sia i dati correlati 
     * ottenuti dalle tabelle 'utente' e 'prodotto' tramite LEFT JOIN.
     * 
     * @param rs Il ResultSet posizionato sul record corrente da leggere
     * @return L'oggetto RecensioneBean avvalorato
     * @throws SQLException Se si verifica un errore durante il recupero delle colonne
     */
    private RecensioneBean mapRow(ResultSet rs) throws SQLException {
        RecensioneBean recensione = new RecensioneBean();
        
        // Mappatura attributi della tabella 'recensione'
        recensione.setIdRecensione(rs.getInt("id_recensione"));
        recensione.setDataRecensione(rs.getTimestamp("data_recensione"));
        recensione.setDescrizione(rs.getString("descrizione"));
        recensione.setValutazione(rs.getDouble("valutazione"));
        recensione.setIdProdotto(rs.getInt("id_prodotto"));
        recensione.setIdUtente(rs.getInt("id_utente"));

        // Mappatura attributi estrapolati in JOIN da 'utente' e 'prodotto'
        recensione.setNomeUtente(rs.getString("nome"));
        recensione.setCognomeUtente(rs.getString("cognome"));
        recensione.setEmailUtente(rs.getString("email"));
        recensione.setNomeProdotto(rs.getString("nome_prodotto"));

        return recensione;
    }
}