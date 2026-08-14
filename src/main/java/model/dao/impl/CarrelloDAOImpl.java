package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import model.bean.CarrelloBean;
import model.dao.CarrelloDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia CarrelloDAO per la gestione della persistenza dei carrelli acquisto.
 * Fornisce metodi per la creazione, il recupero per utente/ID, l'aggiornamento, lo svuotamento del contenuto
 * e l'eliminazione del carrello dal database.
 */
public class CarrelloDAOImpl implements CarrelloDAO {

    // --- QUERY SQL PREPARATE ---

    // Inserimento di un nuovo carrello associato a un determinato utente
    private static final String INSERT_CARRELLO =
        "INSERT INTO carrello (id_utente) VALUES (?)";

    // Selezione del carrello in base all'ID dell'utente proprietario
    private static final String SELECT_BY_UTENTE =
        "SELECT id_carrello, id_utente FROM carrello WHERE id_utente = ?";

    // Selezione del carrello in base al suo identificativo univoco
    private static final String SELECT_BY_ID =
        "SELECT id_carrello, id_utente FROM carrello WHERE id_carrello = ?";

    // Aggiornamento dell'associazione tra il carrello e l'utente
    private static final String UPDATE_CARRELLO =
        "UPDATE carrello SET id_utente = ? WHERE id_carrello = ?";

    // Eliminazione di tutti gli elementi appartenenti a uno specifico carrello (svuotamento)
    private static final String DELETE_CONTENUTO_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ?";

    // Eliminazione definitiva dell'entità carrello
    private static final String DELETE_CARRELLO =
        "DELETE FROM carrello WHERE id_carrello = ?";

    /**
     * Salva un nuovo carrello nel database e assegna all'oggetto Bean l'ID autogenerato.
     * 
     * @param carrello L'oggetto CarrelloBean da persistere
     * @throws SQLException In caso di errore durante l'inserimento
     */
    @Override
    public void doSave(CarrelloBean carrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CARRELLO, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, carrello.getIdUtente());
            ps.executeUpdate();

            // Recupero dell'ID autogenerato (PRIMARY KEY auto-increment) per il carrello
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    carrello.setIdCarrello(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Recupera il carrello associato a uno specifico utente.
     * 
     * @param idUtente L'ID dell'utente proprietario del carrello
     * @return L'oggetto CarrelloBean corrispondente, oppure null se non trovato
     * @throws SQLException In caso di errore durante la query
     */
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

    /**
     * Recupera un carrello in base al suo ID univoco.
     * 
     * @param idCarrello L'ID del carrello da recuperare
     * @return L'oggetto CarrelloBean corrispondente, oppure null se non trovato
     * @throws SQLException In caso di errore durante la query
     */
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

    /**
     * Aggiorna l'utente associato a uno specifico carrello (es. passaggio da carrello ospite ad utente registrato).
     * 
     * @param carrello L'oggetto CarrelloBean con i dati aggiornati
     * @throws SQLException In caso di errore durante l'aggiornamento
     */
    @Override
    public void doUpdate(CarrelloBean carrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CARRELLO)) {

            ps.setInt(1, carrello.getIdUtente());
            ps.setInt(2, carrello.getIdCarrello());

            ps.executeUpdate();
        }
    }

    /**
     * Svuota completamente il carrello rimuovendo tutte le voci di prodotto collegate
     * presenti nella tabella di supporto `contenuto`.
     * 
     * @param idCarrello L'ID del carrello da svuotare
     * @return true se è stata eliminata almeno una riga, false altrimenti
     * @throws SQLException In caso di errore durante la cancellazione dei contenuti
     */
    @Override
    public boolean doEmpty(int idCarrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CONTENUTO_CARRELLO)) {

            ps.setInt(1, idCarrello);
            return ps.executeUpdate() > 0; // Restituisce true se almeno un elemento è stato rimosso
        }
    }

    /**
     * Elimina definitivamente l'entità carrello dal database.
     * 
     * @param idCarrello L'ID del carrello da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     * @throws SQLException In caso di errore durante l'eliminazione
     */
    @Override
    public boolean doDelete(int idCarrello) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_CARRELLO)) {

            ps.setInt(1, idCarrello);
            return ps.executeUpdate() > 0; // Restituisce true se la riga è stata eliminata
        }
    }

    /**
     * Metodo helper privato per mappare la riga corrente del ResultSet 
     * in un oggetto CarrelloBean inizializzando una mappa vuota per i prodotti.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto CarrelloBean popolato
     * @throws SQLException In caso di errore di lettura dei dati
     */
    private CarrelloBean mapRow(ResultSet rs) throws SQLException {
        CarrelloBean carrello = new CarrelloBean();
        carrello.setIdCarrello(rs.getInt("id_carrello"));
        carrello.setIdUtente(rs.getInt("id_utente"));
        // Inizializza la struttura dati interna per accogliere successivamente i prodotti in carrello
        carrello.setProdotti(new HashMap<>());
        return carrello;
    }
}