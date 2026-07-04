package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.OrdineBean;

public interface OrdineDAO {

    /**
     * Salva una riga di dettaglio dell'acquisto nel database.
     * Requisito Checklist: Storicizza il prezzo e l'IVA correnti al momento dell'acquisto.
     * @param ordine L'oggetto contenente l'ID acquisto, ID prodotto, quantità, prezzo di vendita e IVA applicata
     */
    void doSave(OrdineBean ordine) throws SQLException;

    
    /**
     * Recupera tutti i prodotti acquistati all'interno di un determinato acquisto.
     * Requisito Area Cliente/Admin: Visualizzare il dettaglio completo di un singolo ordine con tutti i prodotti.
     * @param idAcquisto ID della transazione principale
     */
    List<OrdineBean> doRetrieveByAcquisto(int idAcquisto) throws SQLException;

    
    /**
     * Aggiorna lo stato di spedizione di un singolo articolo all'interno di un acquisto.
     * Requisito Area Admin: Gestione dello stato dell'ordine (es. 'In elaborazione', 'Spedito', 'Consegnato').
     * @param idAcquisto ID dell'acquisto
     * @param idProdotto ID del prodotto da aggiornare
     * @param nuovoStato La stringa rappresentante il nuovo stato
     */
    boolean doUpdateStatoSpedizione(int idAcquisto, int idProdotto, String nuovoStato) throws SQLException;
}