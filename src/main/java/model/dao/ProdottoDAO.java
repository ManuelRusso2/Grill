package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.ProdottoBean;

public interface ProdottoDAO {

    /**
     * Inserisce un nuovo prodotto nel catalogo
     * Requisito Area Admin: Inserire nuovi prodotti
     */
    void doSave(ProdottoBean prodotto) throws SQLException;

    
    /**
     * Modifica un prodotto esistente
     * Requisito Area Admin: Modificare prodotti esistenti (prezzo, descrizione, IVA, ecc.)
     */
    void doUpdate(ProdottoBean prodotto) throws SQLException;

    
    /**
     * Rende un prodotto non attivo (Cancellazione Logica)
     * Rispetta il vincolo d'integrità referenziale richiesto
     * Requisito Area Admin: Cancellare prodotti dal catalogo
     */
    boolean doDelete(int idProdotto) throws SQLException;

    
    /**
     * Recupera un singolo prodotto tramite la sua Chiave Primaria
     * Requisito Area Cliente: Navigazione del catalogo con visualizzazione dettagliata
     */
    ProdottoBean doRetrieveByKey(int idProdotto) throws SQLException;

    
    /**
     * Restituisce tutti i prodotti attivi per il catalogo pubblico
     * Requisito Area Cliente: Navigazione del catalogo prodotti
     */
    List<ProdottoBean> doRetrieveAllClienti() throws SQLException;

    
    /**
     * Restituisce tutti i prodotti presenti, inclusi quelli non più attivi
     * Requisito Area Admin: Visualizzare l'elenco completo dei prodotti
     */
    List<ProdottoBean> doRetrieveAllAdmin() throws SQLException;

    
    /**
     * Cerca i prodotti attivi il cui nome contiene la stringa inserita
     * Requisito Area Cliente: Barra di ricerca con suggerimenti AJAX
     */
    List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException;
}