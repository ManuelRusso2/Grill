package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.bean.AcquistoBean;
import model.bean.ProdottoBean;
import model.dao.AcquistoDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia AcquistoDAO per la gestione della persistenza 
 * delle transazioni di acquisto e delle operazioni di checkout nel database.
 */
public class AcquistoDAOImpl implements AcquistoDAO {

    /** Aliquota IVA predefinita applicata ai singoli elementi dell'ordine. */
    private static final double IVA_STANDARD = 22.0;

    // =========================================================================
    // QUERY SQL PREPARATE
    // =========================================================================

    /** Inserisce una nuova testata d'acquisto. */
    private static final String INSERT_ACQUISTO =
        "INSERT INTO acquisto (prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente) VALUES (?, ?, ?, ?, ?)";

    /** Aggiorna un record di acquisto esistente. */
    private static final String UPDATE_ACQUISTO =
        "UPDATE acquisto SET prezzo_totale = ?, data_acquisto = ?, metodo_pagamento = ?, indirizzo_consegna = ? WHERE id_acquisto = ?";

    /** Recupera un acquisto specifico tramite il suo identificativo primario. */
    private static final String SELECT_BY_ID =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_acquisto = ?";

    /** Recupera la cronologia degli acquisti effettuati da un determinato utente, ordinati dal più recente. */
    private static final String SELECT_BY_UTENTE =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_utente = ? ORDER BY data_acquisto DESC";

    /** Recupera tutti gli acquisti registrati nel sistema in ordine cronologico decrescente. */
    private static final String SELECT_ALL =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto ORDER BY data_acquisto DESC";

    /** Elimina un acquisto dal database in base all'ID. */
    private static final String DELETE_ACQUISTO =
        "DELETE FROM acquisto WHERE id_acquisto = ?";

    /** Recupera gli acquisti effettuati all'interno di un intervallo di date specificato. */
    private static final String SELECT_BY_DATE_INTERVAL =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE data_acquisto BETWEEN ? AND ? ORDER BY data_acquisto DESC";

    /** Inserisce una singola riga di dettaglio all'interno dell'ordine d'acquisto. */
    private static final String INSERT_ORDINE =
        "INSERT INTO ordine (id_acquisto, id_prodotto, taglia, prezzo_unitario, iva, quantita_acquistata, stato_spedizione) VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Decrementa la giacenza in magazzino di un prodotto a seguito dell'acquisto. */
    private static final String UPDATE_STOCK =
        "UPDATE prodotto SET quantita = quantita - ? WHERE id_prodotto = ?";

    /** Svuota tutti i prodotti contenuti nel carrello specificato. */
    private static final String EMPTY_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ?";

    // =========================================================================
    // METODI CRUD STANDARD
    // =========================================================================

    /**
     * Salva una nuova transazione di acquisto nel database e recupera l'ID autogenerato.
     * 
     * @param acquisto Il bean contenente i dati dell'acquisto da persistere
     * @throws SQLException In caso di errori durante l'esecuzione dell'istruzione SQL
     */
    @Override
    public void doSave(AcquistoBean acquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO, Statement.RETURN_GENERATED_KEYS)) {

            // Impostazione dei parametri della query
            ps.setDouble(1, acquisto.getPrezzoTotale());
            ps.setTimestamp(2, acquisto.getDataAcquisto());
            ps.setString(3, acquisto.getMetodoPagamento());
            ps.setString(4, acquisto.getIndirizzoConsegna());
            ps.setInt(5, acquisto.getIdUtente());

            ps.executeUpdate();

            // Recupera la chiave primaria generata automaticamente dal database
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    acquisto.setIdAcquisto(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Aggiorna i dati di un acquisto già presente nel database.
     * 
     * @param acquisto Il bean contenente le informazioni aggiornate
     * @throws SQLException In caso di errori durante l'esecuzione dell'istruzione SQL
     */
    @Override
    public void doUpdate(AcquistoBean acquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_ACQUISTO)) {

            ps.setDouble(1, acquisto.getPrezzoTotale());
            ps.setTimestamp(2, acquisto.getDataAcquisto());
            ps.setString(3, acquisto.getMetodoPagamento());
            ps.setString(4, acquisto.getIndirizzoConsegna());
            ps.setInt(5, acquisto.getIdAcquisto());

            ps.executeUpdate();
        }
    }

    /**
     * Ricerca un acquisto tramite il suo ID univoco.
     * 
     * @param idAcquisto L'ID dell'acquisto da cercare
     * @return L'oggetto {@link AcquistoBean} corrispondente, o {@code null} se non trovato
     * @throws SQLException In caso di errori durante la consultazione del database
     */
    @Override
    public AcquistoBean doRetrieveById(int idAcquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idAcquisto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Recupera la lista di tutti gli acquisti associati ad un determinato utente.
     * 
     * @param idUtente L'ID dell'utente di cui cercare la cronologia acquisti
     * @return Una lista di {@link AcquistoBean} ordinata dal più recente
     * @throws SQLException In caso di errori di consultazione del DB
     */
    @Override
    public List<AcquistoBean> doRetrieveByUtente(int idUtente) throws SQLException {
        List<AcquistoBean> acquisti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    acquisti.add(mapRow(rs));
                }
            }
        }
        return acquisti;
    }

    /**
     * Recupera la lista completa di tutti gli acquisti effettuati nel sistema.
     * 
     * @return Lista contenente tutti i record presenti nella tabella 'acquisto'
     * @throws SQLException In caso di errore durante la query
     */
    @Override
    public List<AcquistoBean> doRetrieveAll() throws SQLException {
        List<AcquistoBean> acquisti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                acquisti.add(mapRow(rs));
            }
        }
        return acquisti;
    }

    /**
     * Rimuove un acquisto dal database specificandone l'ID.
     * 
     * @param idAcquisto L'identificativo dell'acquisto da cancellare
     * @return {@code true} se la cancellazione è andata a buon fine, {@code false} altrimenti
     * @throws SQLException In caso di errore durante l'esecuzione dell'istruzione SQL
     */
    @Override
    public boolean doDelete(int idAcquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_ACQUISTO)) {

            ps.setInt(1, idAcquisto);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Filtra e restituisce la lista degli acquisti compresi tra due date.
     * 
     * @param dallaData Data d'inizio dell'intervallo temporale
     * @param allaData Data di fine dell'intervallo temporale
     * @return Lista di acquisti registrati nell'intervallo indicato
     * @throws SQLException In caso di errore nell'interrogazione
     */
    @Override
    public List<AcquistoBean> doRetrieveByDateInterval(Date dallaData, Date allaData) throws SQLException {
        List<AcquistoBean> acquisti = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_DATE_INTERVAL)) {

            ps.setDate(1, dallaData);
            ps.setDate(2, allaData);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    acquisti.add(mapRow(rs));
                }
            }
        }
        return acquisti;
    }

    // =========================================================================
    // TRANSAZIONE DI CHECKOUT E TRANSAZIONI COMPLESSE
    // =========================================================================

    /**
     * Esegue l'intera procedura transazionale per il completamento di un acquisto (Checkout).
     * <p>
     * Le operazioni eseguite atomicamente includono:
     * <ol>
     *   <li>Creazione del record di testata nella tabella 'acquisto'.</li>
     *   <li>Inserimento dei singoli articoli nell'ordine (tabella 'ordine').</li>
     *   <li>Aggiornamento delle giacenze in magazzino dei prodotti acquistati.</li>
     *   <li>Svuotamento dei prodotti dal carrello dell'utente.</li>
     * </ol>
     * 
     * @param idCarrello L'ID del carrello da svuotare
     * @param idUtente L'ID dell'utente che conclude l'ordine
     * @param metodoPagamento Il metodo di pagamento scelto
     * @param indirizzoConsegna L'indirizzo di destinazione per la spedizione
     * @param prodottiInCarrello Mappa contenente le coppie Prodotto-Quantità ordinate
     * @param totale Importo complessivo calcolato dell'ordine
     * @return L'ID autogenerato del nuovo acquisto completato
     * @throws SQLException In caso di fallimento di una delle fasi transazionali (triggera il Rollback)
     */
    @Override
    public int completaAcquisto(int idCarrello, int idUtente, String metodoPagamento,
                                String indirizzoConsegna, Map<ProdottoBean, Integer> prodottiInCarrello,
                                double totale) throws SQLException {

        try (Connection con = ConnessioneDB.getConnection()) {
            // Disabilita l'autocommit per avviare una transazione atomica manuale
            con.setAutoCommit(false);

            try {
                int idAcquisto;

                // -----------------------------------------------------------------
                // 1. Inserimento testata dell'acquisto
                // -----------------------------------------------------------------
                try (PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, totale);
                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    ps.setString(3, metodoPagamento);
                    ps.setString(4, indirizzoConsegna);
                    ps.setInt(5, idUtente);
                    ps.executeUpdate();

                    // Recupera l'ID generato per agganciare le righe dell'ordine
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Impossibile recuperare l'ID dell'acquisto.");
                        }
                        idAcquisto = rs.getInt(1);
                    }
                }

                // -----------------------------------------------------------------
                // 2. Inserimento dei dettagli ordine (Batch Execution)
                // -----------------------------------------------------------------
                try (PreparedStatement psOrdine = con.prepareStatement(INSERT_ORDINE)) {
                    for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
                        ProdottoBean prodotto = entry.getKey();
                        int quantita = entry.getValue();

                        // Gestione taglia senza operatore ternario
                        String taglia = prodotto.getTagliaSelezionata();
                        if (taglia == null) {
                            taglia = "Unica";
                        }

                        psOrdine.setInt(1, idAcquisto);
                        psOrdine.setInt(2, prodotto.getIdProdotto());
                        psOrdine.setString(3, taglia);
                        psOrdine.setDouble(4, prodotto.getCosto());
                        psOrdine.setDouble(5, IVA_STANDARD);
                        psOrdine.setInt(6, quantita);
                        psOrdine.setString(7, "In elaborazione");

                        // Aggiunge la query al batch per un'esecuzione ottimizzata
                        psOrdine.addBatch();
                    }
                    psOrdine.executeBatch();
                }

                // -----------------------------------------------------------------
                // 3. Aggiornamento stock magazzino
                // -----------------------------------------------------------------
                try (PreparedStatement psUpdateQty = con.prepareStatement(UPDATE_STOCK)) {
                    for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
                        ProdottoBean prodotto = entry.getKey();
                        int quantitaAcquistata = entry.getValue();

                        // Decrementa le quantità disponibili nel DB
                        psUpdateQty.setInt(1, quantitaAcquistata);
                        psUpdateQty.setInt(2, prodotto.getIdProdotto());
                        psUpdateQty.executeUpdate();
                    }
                }

                // -----------------------------------------------------------------
                // 4. Svuotamento dei prodotti dal carrello
                // -----------------------------------------------------------------
                try (PreparedStatement psEmpty = con.prepareStatement(EMPTY_CARRELLO)) {
                    psEmpty.setInt(1, idCarrello);
                    psEmpty.executeUpdate();
                }

                // Se tutte le operazioni hanno avuto successo, applica le modifiche al DB
                con.commit();
                return idAcquisto;

            } catch (SQLException e) {
                // In caso di errore in qualsiasi punto, annulla l'intera operazione
                con.rollback();
                throw e;
            } finally {
                // Ripristina l'autocommit alle impostazioni di default per la connessione
                con.setAutoCommit(true);
            }
        }
    }

    // =========================================================================
    // METODI HELPER PRIVATI
    // =========================================================================

    /**
     * Mappa la riga corrente di un {@link ResultSet} in un oggetto {@link AcquistoBean}.
     * 
     * @param rs Il ResultSet posizionato sulla riga da leggere
     * @return L'oggetto {@link AcquistoBean} popolato
     * @throws SQLException Se si verifica un errore durante l'estrazione dai campi
     */
    private AcquistoBean mapRow(ResultSet rs) throws SQLException {
        AcquistoBean acquisto = new AcquistoBean();
        acquisto.setIdAcquisto(rs.getInt("id_acquisto"));
        acquisto.setPrezzoTotale(rs.getDouble("prezzo_totale"));
        acquisto.setDataAcquisto(rs.getTimestamp("data_acquisto"));
        acquisto.setMetodoPagamento(rs.getString("metodo_pagamento"));
        acquisto.setIndirizzoConsegna(rs.getString("indirizzo_consegna"));
        acquisto.setIdUtente(rs.getInt("id_utente"));
        return acquisto;
    }
}