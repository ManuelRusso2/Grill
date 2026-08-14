package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.bean.AcquistoBean;
import model.dao.AcquistoDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia AcquistoDAO per la gestione della persistenza degli acquisti/ordini generali.
 * Fornisce metodi per il salvataggio di nuove transazioni, la ricerca per utente o intervallo temporale,
 * l'aggiornamento dei dati di consegna/pagamento e l'eliminazione.
 */
public class AcquistoDAOImpl implements AcquistoDAO {

    // --- QUERY SQL PREPARATE ---

    // Inserimento di una nuova transazione d'acquisto (id_acquisto è generato automaticamente)
    private static final String INSERT_ACQUISTO =
        "INSERT INTO acquisto (prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente) VALUES (?, ?, ?, ?, ?)";

    // Aggiornamento dei dati relativi a un acquisto esistente
    private static final String UPDATE_ACQUISTO =
        "UPDATE acquisto SET prezzo_totale = ?, data_acquisto = ?, metodo_pagamento = ?, indirizzo_consegna = ? WHERE id_acquisto = ?";

    // Selezione di un singolo acquisto tramite il suo ID
    private static final String SELECT_BY_ID =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_acquisto = ?";

    // Selezione dello storico acquisti di un determinato utente, dal più recente al meno recente
    private static final String SELECT_BY_UTENTE =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE id_utente = ? ORDER BY data_acquisto DESC";

    // Selezione di tutti gli acquisti presenti a sistema, ordinati per data decrescente
    private static final String SELECT_ALL =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto ORDER BY data_acquisto DESC";

    // Eliminazione di un acquisto dal database
    private static final String DELETE_ACQUISTO =
        "DELETE FROM acquisto WHERE id_acquisto = ?";

    // Selezione degli acquisti effettuati all'interno di un intervallo di date specificato
    private static final String SELECT_BY_DATE_INTERVAL =
        "SELECT id_acquisto, prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente FROM acquisto WHERE data_acquisto BETWEEN ? AND ? ORDER BY data_acquisto DESC";

    /**
     * Salva una nuova transazione d'acquisto nel database e assegna all'oggetto Bean l'ID autogenerato.
     * 
     * @param acquisto L'oggetto AcquistoBean da registrare
     * @throws SQLException In caso di errore durante l'inserimento
     */
    @Override
    public void doSave(AcquistoBean acquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, acquisto.getPrezzoTotale());
            ps.setTimestamp(2, acquisto.getDataAcquisto());
            ps.setString(3, acquisto.getMetodoPagamento());
            ps.setString(4, acquisto.getIndirizzoConsegna());
            ps.setInt(5, acquisto.getIdUtente());

            ps.executeUpdate();

            // Recupero della chiave primaria autogenerata (id_acquisto) dal database
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    acquisto.setIdAcquisto(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Aggiorna i dettagli di un acquisto già presente nel database (es. correzione indirizzo o totale).
     * 
     * @param acquisto L'oggetto AcquistoBean con i dati aggiornati
     * @throws SQLException In caso di errore durante l'aggiornamento
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
     * Cerca e restituisce una transazione d'acquisto in base al suo ID univoco.
     * 
     * @param idAcquisto L'ID dell'acquisto da cercare
     * @return L'oggetto AcquistoBean trovato, oppure null se non presente
     * @throws SQLException In caso di errore durante la query
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
     * Recupera lo storico completo degli acquisti effettuati da uno specifico utente,
     * ordinati dal più recente al più datato.
     * 
     * @param idUtente L'ID dell'utente di cui recuperare le transazioni
     * @return Lista di oggetti AcquistoBean associati all'utente
     * @throws SQLException In caso di errore durante la query
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
     * Recupera la lista di tutti gli acquisti presenti a sistema (utilizzato principalmente per report o gestione admin).
     * 
     * @return Lista contenente tutti gli oggetti AcquistoBean presenti
     * @throws SQLException In caso di errore durante la lettura
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
     * Rimuove un record di acquisto dal database tramite il suo ID.
     * 
     * @param idAcquisto L'ID dell'acquisto da eliminare
     * @return true se l'eliminazione ha modificato almeno una riga, false altrimenti
     * @throws SQLException In caso di errore durante la cancellazione
     */
    @Override
    public boolean doDelete(int idAcquisto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_ACQUISTO)) {

            ps.setInt(1, idAcquisto);
            return ps.executeUpdate() > 0; // Restituisce true se la rimozione è andata a buon fine
        }
    }

    /**
     * Recupera tutti gli acquisti effettuati compresi tra due date specificate (estremi inclusi).
     * 
     * @param dallaData Data di inizio intervallo (inclusa)
     * @param allaData  Data di fine intervallo (inclusa)
     * @return Lista di oggetti AcquistoBean ricadenti nel range temporale
     * @throws SQLException In caso di errore durante l'esecuzione della query
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

    /**
     * Metodo helper privato per la mappatura dei campi del ResultSet 
     * in un nuovo oggetto AcquistoBean.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto AcquistoBean popolato
     * @throws SQLException In caso di errore nell'estrazione dei valori dalle colonne
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