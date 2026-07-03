package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.RecensioneBean;

public interface RecensioneDAO {

    /**
     * Inserisce una nuova recensione/valutazione per un prodotto.
     * @param recensione L'oggetto contenente il voto (0.5 - 5.0), la descrizione, l'ID utente e l'ID prodotto
     */
    void doSave(RecensioneBean recensione) throws SQLException;

    
    /**
     * Modifica una recensione esistente
     */
    void doUpdate(RecensioneBean recensione) throws SQLException;

    
    /**
     * Recupera una singola recensione tramite la sua Chiave Primaria.
     */
    RecensioneBean doRetrieveById(int idRecensione) throws SQLException;

    
    /**
     * Recupera tutte le recensioni associate a un determinato prodotto.
     * @param idProdotto ID del prodotto di cui mostrare le recensioni
     */
    List<RecensioneBean> doRetrieveByProdotto(int idProdotto) throws SQLException;

    
    /**
     * Recupera tutte le recensioni scritte da un singolo utente.
     * @param idUtente ID dell'utente recensore
     */
    List<RecensioneBean> doRetrieveByUtente(int idUtente) throws SQLException;

    
    /**
     * Restituisce l'elenco complessivo di tutte le recensioni del sito.
     * Requisito Area Admin: Monitoraggio e moderazione dei commenti degli utenti.
     */
    List<RecensioneBean> doRetrieveAll() throws SQLException;

    
    /**
     * Elimina fisicamente una recensione dal database.
     * Requisito Area Admin: Rimozione di recensioni inappropriate o spam.
     * @param idRecensione ID della recensione da rimuovere
     */
    boolean doDelete(int idRecensione) throws SQLException;
}