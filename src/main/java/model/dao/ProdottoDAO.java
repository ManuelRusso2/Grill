package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.ProdottoBean;

/**
 * Interfaccia DAO (Data Access Object) per la gestione della persistenza dei dati dei prodotti.
 * Definisce i contratti per le operazioni CRUD, le interrogazioni del catalogo di vendita,
 * l'accorpamento delle varianti di prodotto, la ricerca asincrona live (AJAX) e la gestione
 * dei prodotti riservata al pannello di amministrazione.
 */
public interface ProdottoDAO {

    /**
     * Salva un nuovo prodotto nel database e gestisce le relative associazioni con le categorie.
     * 
     * @param prodotto L'oggetto {@link ProdottoBean} contenente i dati del nuovo prodotto da inserire
     * @throws SQLException Se si verifica un errore durante l'esecuzione dell'inserimento o della transazione
     */
    void doSave(ProdottoBean prodotto) throws SQLException;

    /**
     * Aggiorna i dati di un prodotto esistente nel database e ne sincronizza le categorie associate.
     * 
     * @param prodotto L'oggetto {@link ProdottoBean} con le informazioni aggiornate
     * @throws SQLException Se si verifica un errore durante l'aggiornamento dei dati
     */
    void doUpdate(ProdottoBean prodotto) throws SQLException;

    /**
     * Esegue la disattivazione logica (Soft Delete) di un prodotto tramite il suo ID,
     * garantendo la conservazione dello storico negli ordini passati.
     * 
     * @param idProdotto L'identificativo univoco del prodotto da disattivare
     * @return {@code true} se la disattivazione ha avuto successo, {@code false} altrimenti
     * @throws SQLException Se si verifica un errore durante l'aggiornamento dello stato del prodotto
     */
    boolean doDelete(int idProdotto) throws SQLException;

    /**
     * Ricerca e restituisce un singolo prodotto tramite la sua chiave primaria (ID).
     * 
     * @param idProdotto L'ID univoco del prodotto da recuperare
     * @return L'oggetto {@link ProdottoBean} corrispondente se trovato, oppure {@code null}
     * @throws SQLException Se si verifica un errore di accesso al database
     */
    ProdottoBean doRetrieveByKey(int idProdotto) throws SQLException;

    /**
     * Recupera l'elenco di tutti i prodotti attualmente attivi sul sito e disponibili per l'acquisto.
     * 
     * @return Una {@link List} di oggetti {@link ProdottoBean} contenente solo i prodotti attivi
     * @throws SQLException Se si verifica un errore durante la lettura dei dati
     */
    List<ProdottoBean> doRetrieveAllProdotti() throws SQLException;

    /**
     * Recupera l'elenco completo dei prodotti (inclusi quelli disattivati) per la gestione nel pannello Admin.
     * 
     * @return Una {@link List} di tutti i prodotti presenti nel database
     * @throws SQLException Se si verifica un errore durante la query
     */
    List<ProdottoBean> doRetrieveAllAdmin() throws SQLException;

    /**
     * Recupera tutti i prodotti attivi appartenenti a una determinata categoria.
     * 
     * @param idCategoria L'ID della categoria su cui applicare il filtro
     * @return Una {@link List} di prodotti associati alla categoria specificata
     * @throws SQLException Se si verifica un errore durante la lettura
     */
    List<ProdottoBean> doRetrieveByCategoria(int idCategoria) throws SQLException;

    /**
     * Esegue una ricerca rapida per la barra di autocompletamento in tempo reale (live search AJAX).
     * Filtra per nome prodotto, descrizione o nome della categoria collegata.
     * 
     * @param query La stringa di ricerca inviata dall'utente
     * @return Una {@link List} limitata di oggetti {@link ProdottoBean} corrispondenti ai criteri
     * @throws SQLException Se si verifica un errore durante la ricerca nel DB
     */
    List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException;

    /**
     * Recupera l'elenco dei prodotti raggruppando le varianti sotto un unico nome base per la vista del catalogo.
     * 
     * @return Una {@link List} di prodotti con dati aggregati e quantita totale calcolata
     * @throws SQLException Se si verifica un errore durante l'aggregazione dei dati
     */
    List<ProdottoBean> doRetrieveAllProdottiRaggruppati() throws SQLException;

    /**
     * Recupera tutte le varianti specifiche collegate a un determinato prodotto base.
     * 
     * @param nomeBase Il nome base/prefisso utilizzato per filtrare le varianti
     * @return Una {@link List} di oggetti {@link ProdottoBean} rappresentanti le varianti del prodotto
     * @throws SQLException Se si verifica un errore durante la selezione delle varianti
     */
    List<ProdottoBean> doRetrieveVarianti(String nomeBase) throws SQLException;
}