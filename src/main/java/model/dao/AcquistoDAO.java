package model.dao;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import model.bean.AcquistoBean;

public interface AcquistoDAO {

    /**
     * Inserisce un nuovo acquisto nel database
     * Requisito Area Cliente: Conferma ordine con svuotamento del carrello.
     * @param acquisto L'oggetto contenente i dati del pagamento, indirizzo e ID utente
     */
    void doSave(AcquistoBean acquisto) throws SQLException;

    
    /**
     * Modifica i dati di un acquisto esistente
     */
    void doUpdate(AcquistoBean acquisto) throws SQLException;

    
    /**
     * Recupera un singolo acquisto tramite il suo ID
     */
    AcquistoBean doRetrieveById(int idAcquisto) throws SQLException;

    
    /**
     * Recupera lo storico degli acquisti effettuati da un determinato utente.
     * Requisito Area Cliente: Visualizzazione dello storico ordini effettuati.
     * Requisito Area Admin: Filtrare gli ordini per cliente.
     * @param idUtente ID del cliente di cui cercare gli ordini
     */
    List<AcquistoBean> doRetrieveByUtente(int idUtente) throws SQLException;

    
    /**
     * Restituisce l'elenco complessivo di tutti gli acquisti effettuati sul sito.
     * Requisito Area Amministratore: Visualizzare gli ordini complessivi.
     */
    List<AcquistoBean> doRetrieveAll() throws SQLException;

    
    /**
     * Elimina un acquisto dal database (o lo imposta come ANNULLATO).
     */
    boolean doDelete(int idAcquisto) throws SQLException;

    
    /**
     * Filtra gli acquisti in un intervallo temporale.
     * Requisito Area Amministratore: Filtrare gli ordini per intervallo di date (dalla data – alla data).
     * @param dallaData Data di inizio intervallo
     * @param allaData Data di fine intervallo
     */
    List<AcquistoBean> doRetrieveByDateInterval(Date dallaData, Date allaData) throws SQLException;
}