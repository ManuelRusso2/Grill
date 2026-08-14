package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.bean.OrdineBean;
import model.dao.OrdineDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia OrdineDAO per la gestione dei dettagli delle singole voci d'ordine.
 * Rappresenta la tabella di giunzione/dettaglio tra la transazione complessiva (Acquisto) 
 * e i singoli elementi acquistati (Prodotti).
 */
public class OrdineDAOImpl implements OrdineDAO {

    // Stato di spedizione di default applicato se non ne viene specificato uno valido
    private static final String DEFAULT_STATO_SPEDIZIONE = "In elaborazione";

    // --- QUERY SQL PREPARATE ---

    // Inserimento di un singolo articolo/riga all'interno di un acquisto
    private static final String INSERT_ORDINE =
        "INSERT INTO ordine (id_acquisto, id_prodotto, prezzo_unitario, iva, quantita_acquistata, stato_spedizione) VALUES (?, ?, ?, ?, ?, ?)";

    // Selezione di tutte le righe d'ordine associate ad uno specifico acquisto
    private static final String SELECT_BY_ACQUISTO =
        "SELECT id_acquisto, id_prodotto, prezzo_unitario, iva, quantita_acquistata, stato_spedizione FROM ordine WHERE id_acquisto = ?";

    // Aggiornamento dello stato di spedizione per uno specifico prodotto all'interno di un acquisto (chiave composta)
    private static final String UPDATE_STATO_SPEDIZIONE =
        "UPDATE ordine SET stato_spedizione = ? WHERE id_acquisto = ? AND id_prodotto = ?";

    /**
     * Salva una singola voce d'ordine nel database.
     * Se lo stato di spedizione non è presente, viene impostato il valore di default ("In elaborazione").
     * 
     * @param ordine L'oggetto OrdineBean contenente i dettagli del prodotto inserito nell'ordine
     * @throws SQLException In caso di errore durante l'inserimento sul database
     */
    @Override
    public void doSave(OrdineBean ordine) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ORDINE)) {

            ps.setInt(1, ordine.getIdAcquisto());
            ps.setInt(2, ordine.getIdProdotto());
            ps.setDouble(3, ordine.getPrezzoUnitario());
            ps.setDouble(4, ordine.getIva());
            ps.setInt(5, ordine.getQuantitaAcquistata());
            
            // Gestione del valore di fallback per lo stato della spedizione
            String stato = ordine.getStatoSpedizione();
            ps.setString(6, (stato != null && !stato.trim().isEmpty()) ? stato : DEFAULT_STATO_SPEDIZIONE);

            ps.executeUpdate();
        }
    }

    /**
     * Recupera tutte le righe d'ordine (prodotti e relative quantità/prezzi) collegate a un determinato acquisto.
     * 
     * @param idAcquisto L'ID della transazione/acquisto generale
     * @return Lista di oggetti OrdineBean associati all'acquisto
     * @throws SQLException In caso di errore di lettura dal database
     */
    @Override
    public List<OrdineBean> doRetrieveByAcquisto(int idAcquisto) throws SQLException {
        List<OrdineBean> dettagli = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ACQUISTO)) {

            ps.setInt(1, idAcquisto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dettagli.add(mapRow(rs));
                }
            }
        }
        return dettagli;
    }

    /**
     * Aggiorna lo stato della spedizione di uno specifico articolo appartenente a un acquisto.
     * Identifica la riga univoca tramite la chiave primaria composta (id_acquisto, id_prodotto).
     * 
     * @param idAcquisto  L'ID dell'acquisto
     * @param idProdotto  L'ID del prodotto coinvolto
     * @param nuovoStato Il nuovo stato di spedizione da applicare
     * @return true se l'aggiornamento ha modificato con successo la riga, false altrimenti
     * @throws SQLException In caso di errore durante l'aggiornamento
     */
    @Override
    public boolean doUpdateStatoSpedizione(int idAcquisto, int idProdotto, String nuovoStato) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_STATO_SPEDIZIONE)) {

            ps.setString(1, nuovoStato);
            ps.setInt(2, idAcquisto);
            ps.setInt(3, idProdotto);

            return ps.executeUpdate() > 0; // Restituisce true se almeno una riga è stata modificata
        }
    }

    /**
     * Metodo helper privato che esegue il mapping da una riga di ResultSet
     * a un oggetto della classe OrdineBean.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto OrdineBean popolato
     * @throws SQLException In caso di errore durante l'estrazione dei campi
     */
    private OrdineBean mapRow(ResultSet rs) throws SQLException {
        OrdineBean ordine = new OrdineBean();
        ordine.setIdAcquisto(rs.getInt("id_acquisto"));
        ordine.setIdProdotto(rs.getInt("id_prodotto"));
        ordine.setPrezzoUnitario(rs.getDouble("prezzo_unitario"));
        ordine.setIva(rs.getDouble("iva"));
        ordine.setQuantitaAcquistata(rs.getInt("quantita_acquistata"));
        ordine.setStatoSpedizione(rs.getString("stato_spedizione"));
        return ordine;
    }
}