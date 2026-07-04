package model.dao;

import java.sql.SQLException;
import java.util.Map;
import model.bean.ProdottoBean;

public interface ContenutoDAO {

    /**
     * Aggiunge un prodotto al carrello o ne incrementa la quantità se già presente.
     * @param idCarrello ID del carrello dell'utente
     * @param idProdotto ID del prodotto da inserire
     * @param quantita Quantità da aggiungere
     */
    void doAddProduct(int idCarrello, int idProdotto, int quantita) throws SQLException;

    
    /**
     * Aggiorna la quantità esatta di un singolo prodotto già presente nel carrello.
     * Utile quando l'utente modifica il numero di articoli direttamente dalla pagina del carrello.
     */
    void doUpdateQuantity(int idCarrello, int idProdotto, int quantita) throws SQLException;

    
    /**
     * Rimuove completamente un singolo prodotto dal carrello.
     * Corrisponde al click sulla "X" o sul pulsante "Rimuovi" nella pagina carrello.
     */
    void doRemoveProduct(int idCarrello, int idProdotto) throws SQLException;

    
    /**
     * Recupera tutti i prodotti presenti nel carrello con le rispettive quantità.
     * Restituisce una Mappa (Prodotto -> Quantità nel carrello) per popolare il CarrelloBean.
     */
    Map<ProdottoBean, Integer> doRetrieveProdottiInCarrello(int idCarrello) throws SQLException;
}