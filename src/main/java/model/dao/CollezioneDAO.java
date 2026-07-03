package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.CollezioneBean;

public interface CollezioneDAO {

    /**
     * Inserisce una nuova collezione nel database
     * @param collezione L'oggetto contenente il nome e la descrizione della nuova collezione
     */
    void doSave(CollezioneBean collezione) throws SQLException;

    
    /**
     * Modifica i dati di una collezione esistente.
     * @param collezione L'oggetto aggiornato con l'ID della collezione da modificare
     */
    void doUpdate(CollezioneBean collezione) throws SQLException;

    
    /**
     * Recupera una singola collezione tramite la sua Chiave Primaria.
     * @param idCollezione ID identificativo della collezione
     */
    CollezioneBean doRetrieveById(int idCollezione) throws SQLException;

    
    /**
     * Restituisce l'elenco completo di tutte le collezioni presenti nel database.
     * Utile sia per l'Admin (per associare un prodotto a una collezione) sia per il Cliente
     */
    List<CollezioneBean> doRetrieveAll() throws SQLException;

    
    /**
     * Elimina fisicamente una collezione dal database.
     * @param idCollezione ID della collezione da eliminare
     */
    boolean doDelete(int idCollezione) throws SQLException;
}