package model.dao;

import java.sql.SQLException;
import model.bean.CarrelloBean;

public interface CarrelloDAO {

    /**
     * Inserisce un nuovo carrello nel database per un utente.
     * Viene chiamato solitamente subito dopo la registrazione di un nuovo utente.
     * @param carrello L'oggetto contenente l'ID dell'utente associato
     */
    void doSave(CarrelloBean carrello) throws SQLException;

    
    /**
     * Recupera il carrello associato a un determinato utente tramite il suo ID.
     * Requisito Area Cliente: Gestione del carrello al login.
     * @param idUtente ID dell'utente proprietario del carrello
     */
    CarrelloBean doRetrieveByUtente(int idUtente) throws SQLException;

    
    /**
     * Recupera un carrello dal database tramite la sua Chiave Primaria.
     */
    CarrelloBean doRetrieveById(int idCarrello) throws SQLException;

    
    /**
     * Aggiorna i dati generali del carrello
     */
    void doUpdate(CarrelloBean carrello) throws SQLException;

    
    /**
     * Svuota completamente il contenuto del carrello (rimuove tutti i prodotti associati).
     * Requisito Area Cliente: Conferma ordine con SVUOTAMENTO del carrello.
     * @param idCarrello ID del carrello da svuotare
     */
    boolean doEmpty(int idCarrello) throws SQLException;

    
    /**
     * Elimina fisicamente il carrello di un utente dal database.
     */
    boolean doDelete(int idCarrello) throws SQLException;
}