package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.bean.CollezioneBean;
import model.dao.CollezioneDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia CollezioneDAO per la gestione delle collezioni di prodotti nel database.
 * Fornisce le operazioni CRUD (Create, Read, Update, Delete) sulla tabella "collezione".
 */
public class CollezioneDAOImpl implements CollezioneDAO {

    // --- QUERY SQL PREPARATE ---

    // Inserimento di una nuova collezione (id_collezione e data_creazione sono gestiti dal DB)
    private static final String INSERT_COLLEZIONE =
        "INSERT INTO collezione (nome_collezione, descrizione) VALUES (?, ?)";

    // Aggiornamento dei dati di una collezione esistente
    private static final String UPDATE_COLLEZIONE =
        "UPDATE collezione SET nome_collezione = ?, descrizione = ? WHERE id_collezione = ?";

    // Selezione di una singola collezione tramite identificativo
    private static final String SELECT_BY_ID =
        "SELECT id_collezione, nome_collezione, descrizione, data_creazione FROM collezione WHERE id_collezione = ?";

    // Selezione di tutte le collezioni ordinate alfabeticamente per nome
    private static final String SELECT_ALL =
        "SELECT id_collezione, nome_collezione, descrizione, data_creazione FROM collezione ORDER BY nome_collezione";

    // Eliminazione di una collezione tramite identificativo
    private static final String DELETE_COLLEZIONE =
        "DELETE FROM collezione WHERE id_collezione = ?";

    /**
     * Salva una nuova collezione nel database e recupera l'ID autogenerato.
     * 
     * @param collezione L'oggetto CollezioneBean da persistere
     * @throws SQLException In caso di errore durante l'inserimento
     */
    @Override
    public void doSave(CollezioneBean collezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_COLLEZIONE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, collezione.getNomeCollezione());
            ps.setString(2, collezione.getDescrizione());

            ps.executeUpdate();

            // Recupero dell'ID autogenerato dalla chiave primaria auto-increment
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    collezione.setIdCollezione(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Aggiorna il nome e la descrizione di una collezione esistente nel database.
     * 
     * @param collezione L'oggetto CollezioneBean con i dati aggiornati
     * @throws SQLException In caso di errore durante l'aggiornamento
     */
    @Override
    public void doUpdate(CollezioneBean collezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_COLLEZIONE)) {

            ps.setString(1, collezione.getNomeCollezione());
            ps.setString(2, collezione.getDescrizione());
            ps.setInt(3, collezione.getIdCollezione());

            ps.executeUpdate();
        }
    }

    /**
     * Cerca e restituisce una collezione in base al suo ID univoco.
     * 
     * @param idCollezione L'identificativo della collezione da cercare
     * @return L'oggetto CollezioneBean se trovato, null altrimenti
     * @throws SQLException In caso di errore durante la query
     */
    @Override
    public CollezioneBean doRetrieveById(int idCollezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, idCollezione);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Recupera l'elenco completo di tutte le collezioni presenti a catalogo, ordinate per nome.
     * 
     * @return Una lista di oggetti CollezioneBean
     * @throws SQLException In caso di errore durante la query
     */
    @Override
    public List<CollezioneBean> doRetrieveAll() throws SQLException {
        List<CollezioneBean> collezioni = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                collezioni.add(mapRow(rs));
            }
        }
        return collezioni;
    }

    /**
     * Rimuove una collezione dal database in base al suo ID.
     * 
     * @param idCollezione L'ID della collezione da eliminare
     * @return true se l'eliminazione ha rimosso almeno una riga, false altrimenti
     * @throws SQLException In caso di errore durante l'eliminazione
     */
    @Override
    public boolean doDelete(int idCollezione) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_COLLEZIONE)) {

            ps.setInt(1, idCollezione);
            return ps.executeUpdate() > 0; // Restituisce true se l'eliminazione ha avuto effetto
        }
    }

    /**
     * Metodo helper privato per eseguire il mapping tra la riga del ResultSet 
     * e un nuovo oggetto CollezioneBean.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto CollezioneBean popolato
     * @throws SQLException In caso di errore durante la lettura dei campi
     */
    private CollezioneBean mapRow(ResultSet rs) throws SQLException {
        CollezioneBean collezione = new CollezioneBean();
        collezione.setIdCollezione(rs.getInt("id_collezione"));
        collezione.setNomeCollezione(rs.getString("nome_collezione"));
        collezione.setDescrizione(rs.getString("descrizione"));
        collezione.setDataCreazione(rs.getTimestamp("data_creazione"));
        return collezione;
    }
}