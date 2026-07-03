package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.CategoriaBean;

public interface CategoriaDAO {

    /**
     * Inserisce una nuova categoria nel database.
     * @param categoria L'oggetto contenente il nome e la descrizione della nuova categoria
     */
    void doSave(CategoriaBean categoria) throws SQLException;

    
    /**
     * Modifica i dati di una categoria esistente (es. cambio nome o descrizione).
     * @param categoria L'oggetto aggiornato con l'ID della categoria da modificare
     */
    void doUpdate(CategoriaBean categoria) throws SQLException;

    
    /**
     * Recupera una singola categoria tramite la sua Chiave Primaria.
     * Utile per mostrare il nome della categoria selezionata nella pagina del catalogo.
     * @param idCategoria ID identificativo della categoria
     */
    CategoriaBean doRetrieveById(int idCategoria) throws SQLException;

    
    /**
     * Restituisce l'elenco completo di tutte le categorie presenti nel database.
     * Requisito Area Cliente: Utile per navigare il catalogo.
     * Requisito Area Admin: Mostrare la lista per modifiche o inserimenti.
     */
    List<CategoriaBean> doRetrieveAll() throws SQLException;

    
    /**
     * Elimina fisicamente una categoria dal database.
     * @param idCategoria ID della categoria da eliminare
     */
    boolean doDelete(int idCategoria) throws SQLException;
}