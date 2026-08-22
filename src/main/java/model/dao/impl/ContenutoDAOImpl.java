package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.bean.ProdottoBean;
import model.dao.ContenutoDAO;
import utility.ConnessioneDB;

/**
 * Implementazione dell'interfaccia ContenutoDAO per la gestione dei prodotti all'interno del carrello.
 * Gestisce l'aggiunta, la modifica della quantità, la rimozione e i controlli di disponibilità a magazzino,
 * garantendo il rispetto delle giacenze totali indipendentemente dalle taglie selezionate.
 */
public class ContenutoDAOImpl implements ContenutoDAO {

    // =========================================================================
    // QUERY SQL PREPARATE
    // =========================================================================

    /** Recupera la quantità di uno specifico prodotto e taglia presente nel carrello. */
    private static final String SELECT_SINGOLO_PRODOTTO =
        "SELECT quantita FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    /** Calcola la somma totale delle quantità di un prodotto nel carrello a prescindere dalla taglia. */
    private static final String SELECT_TOTALE_PRODOTTO_CARRELLO =
        "SELECT COALESCE(SUM(quantita), 0) FROM contenuto WHERE id_carrello = ? AND id_prodotto = ?";

    /** Inserimento di una nuova combinazione prodotto/taglia nel carrello. */
    private static final String INSERT_PRODOTTO_CARRELLO =
        "INSERT INTO contenuto (id_carrello, id_prodotto, quantita, taglia) VALUES (?, ?, ?, ?)";

    /** Aggiornamento della quantità per un elemento specifico del carrello. */
    private static final String UPDATE_QUANTITA_CARRELLO =
        "UPDATE contenuto SET quantita = ? WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    /** Eliminazione di una riga specifica (prodotto e taglia) dal carrello. */
    private static final String DELETE_PRODOTTO_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    /** Lettura dello stock fisico complessivo disponibile a magazzino per un dato prodotto. */
    private static final String SELECT_QUANTITA_DISPONIBILE =
        "SELECT quantita FROM prodotto WHERE id_prodotto = ?";

    /** Estrazione completa dei prodotti nel carrello in JOIN con la tabella prodotto. */
    private static final String SELECT_PRODOTTI_JOIN =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.quantita AS disponibilita, p.attivo, p.id_collezione, c.quantita AS quantita_carrello, c.taglia " +
        "FROM contenuto c JOIN prodotto p ON c.id_prodotto = p.id_prodotto WHERE c.id_carrello = ?";

    // =========================================================================
    // METODI DELL'INTERFACCIA (METODI PUBBLICI)
    // =========================================================================

    /**
     * Aggiunge una quantità di un prodotto con una specifica taglia al carrello.
     * Effettua la somma di TUTTE le taglie già in carrello per verificare che l'operazione
     * non superi lo stock totale a magazzino (evitando problemi di overselling).
     *
     * @param idCarrello ID del carrello dell'utente
     * @param idProdotto ID del prodotto da inserire
     * @param quantita   Quantità che l'utente intende aggiungere
     * @param taglia     Taglia selezionata
     * @throws SQLException In caso di errore durante l'interrogazione del database
     */
    @Override
    public void doAddProduct(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            
            // 1. Controlla la quantità già presente nel carrello per QUESTA specifica taglia
            int quantitaEsistenteTaglia = 0;
            try (PreparedStatement psCheck = con.prepareStatement(SELECT_SINGOLO_PRODOTTO)) {
                psCheck.setInt(1, idCarrello);
                psCheck.setInt(2, idProdotto);
                psCheck.setString(3, taglia);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        quantitaEsistenteTaglia = rs.getInt("quantita");
                    }
                }
            }

            // 2. Recupera la disponibilità massima reale nel magazzino
            int quantitaDisponibile = getQuantitaDisponibile(con, idProdotto);

            // 3. Calcola il totale complessivo dello stesso articolo già a carrello (tutte le taglie sommate)
            int quantitaTotaleInCarrello = getQuantitaTotaleProdottoInCarrello(con, idCarrello, idProdotto);

            // 4. Controllo di sicurezza: interrompe se l'aggiunta supera lo stock totale
            if (quantitaTotaleInCarrello + quantita > quantitaDisponibile) {
                return;
            }

            // 5. Aggiorna la voce esistente se la taglia c'era già, altrimenti inserisce una nuova riga
            if (quantitaEsistenteTaglia > 0) {
                int nuovaQuantitaTaglia = quantitaEsistenteTaglia + quantita;
                try (PreparedStatement psUpdate = con.prepareStatement(UPDATE_QUANTITA_CARRELLO)) {
                    psUpdate.setInt(1, nuovaQuantitaTaglia);
                    psUpdate.setInt(2, idCarrello);
                    psUpdate.setInt(3, idProdotto);
                    psUpdate.setString(4, taglia);
                    psUpdate.executeUpdate();
                }
            } else {
                try (PreparedStatement psInsert = con.prepareStatement(INSERT_PRODOTTO_CARRELLO)) {
                    psInsert.setInt(1, idCarrello);
                    psInsert.setInt(2, idProdotto);
                    psInsert.setInt(3, quantita);
                    psInsert.setString(4, taglia);
                    psInsert.executeUpdate();
                }
            }
        }
    }

    /**
     * Aggiorna la quantità esatta di un singolo elemento nel carrello.
     * Verifica che il nuovo totale (sommato ad eventuali altre taglie dello stesso prodotto)
     * rimanga nei limiti delle giacenze.
     *
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto da aggiornare
     * @param quantita   Nuovo valore di quantità da impostare
     * @param taglia     Taglia dell'elemento in modifica
     * @throws SQLException In caso di errore durante l'aggiornamento SQL
     */
    @Override
    public void doUpdateQuantity(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            int quantitaDisponibile = getQuantitaDisponibile(con, idProdotto);

            // Legge la quantità prima della modifica per questa specifica taglia
            int quantitaAttualeTaglia = 0;
            try (PreparedStatement psCheck = con.prepareStatement(SELECT_SINGOLO_PRODOTTO)) {
                psCheck.setInt(1, idCarrello);
                psCheck.setInt(2, idProdotto);
                psCheck.setString(3, taglia);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        quantitaAttualeTaglia = rs.getInt("quantita");
                    }
                }
            }

            // Calcola la somma occupata dalle ALTRE taglie dello stesso prodotto
            int totaleAltreTaglie = getQuantitaTotaleProdottoInCarrello(con, idCarrello, idProdotto) - quantitaAttualeTaglia;

            // Se la nuova quantità più le altre taglie supera lo stock a magazzino, annulla l'aggiornamento
            if (totaleAltreTaglie + quantita > quantitaDisponibile) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(UPDATE_QUANTITA_CARRELLO)) {
                ps.setInt(1, quantita);
                ps.setInt(2, idCarrello);
                ps.setInt(3, idProdotto);
                ps.setString(4, taglia);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Rimuove definitivamente una riga specifica (coppia prodotto + taglia) dal carrello.
     *
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto da rimuovere
     * @param taglia     Taglia del prodotto da eliminare
     * @throws SQLException In caso di errore durante l'eliminazione SQL
     */
    @Override
    public void doRemoveProduct(int idCarrello, int idProdotto, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_PRODOTTO_CARRELLO)) {

            ps.setInt(1, idCarrello);
            ps.setInt(2, idProdotto);
            ps.setString(3, taglia);
            ps.executeUpdate();
        }
    }

    /**
     * Recupera la lista dei prodotti presenti nel carrello sotto forma di Mappa.
     *
     * @param idCarrello ID del carrello da esaminare
     * @return Una Map con ProdottoBean come chiave (inclusa la taglia selezionata) e l'intero come quantità
     * @throws SQLException In caso di errore durante la query di JOIN
     */
    @Override
    public Map<ProdottoBean, Integer> doRetrieveProdottiInCarrello(int idCarrello) throws SQLException {
        Map<ProdottoBean, Integer> mappaCarrello = new HashMap<>();

        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_PRODOTTI_JOIN)) {

            ps.setInt(1, idCarrello);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProdottoBean prodotto = mapProdotto(rs);
                    int quantitaNelCarrello = rs.getInt("quantita_carrello");
                    mappaCarrello.put(prodotto, quantitaNelCarrello);
                }
            }
        }
        return mappaCarrello;
    }

    /**
     * Calcola il numero totale di pezzi nel carrello per un determinato ID prodotto,
     * effettuando la somma di tutte le taglie inserite dall'utente.
     *
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto da verificare
     * @return Somma intera delle quantità per quell'ID prodotto nel carrello
     * @throws SQLException In caso di errore di lettura dal DB
     */
    @Override
    public int getQuantitaTotaleProdottoInCarrello(int idCarrello, int idProdotto) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            return getQuantitaTotaleProdottoInCarrello(con, idCarrello, idProdotto);
        }
    }

    // =========================================================================
    // METODI HELPER PRIVATI
    // =========================================================================

    /**
     * Helper interno per eseguire la query SUM sulla tabella contenuto riutilizzando una connessione attiva.
     *
     * @param con        Connessione SQL corrente
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto
     * @return Totale pezzi presenti a carrello
     * @throws SQLException In caso di errore SQL
     */
    private int getQuantitaTotaleProdottoInCarrello(Connection con, int idCarrello, int idProdotto) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_TOTALE_PRODOTTO_CARRELLO)) {
            ps.setInt(1, idCarrello);
            ps.setInt(2, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Helper interno per recuperare la quantità fisica a magazzino dalla tabella prodotto.
     *
     * @param con        Connessione SQL corrente
     * @param idProdotto ID del prodotto da controllare
     * @return Giacenza magazzino disponibile
     * @throws SQLException In caso di errore SQL
     */
    private int getQuantitaDisponibile(Connection con, int idProdotto) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_QUANTITA_DISPONIBILE)) {
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantita");
                }
            }
        }
        return 0;
    }

    /**
     * Helper per mappare le colonne estratte dalla JOIN in un oggetto di tipo ProdottoBean.
     *
     * @param rs ResultSet posizionato sul record corrente
     * @return Oggetto ProdottoBean valorizzato con i dati e la taglia selezionata
     * @throws SQLException In caso di errore di lettura dei dati
     */
    private ProdottoBean mapProdotto(ResultSet rs) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setIdProdotto(rs.getInt("id_prodotto"));
        prodotto.setNome(rs.getString("nome"));
        prodotto.setDescrizione(rs.getString("descrizione"));
        prodotto.setCosto(rs.getDouble("costo"));
        prodotto.setQuantita(rs.getInt("disponibilita"));
        prodotto.setAttivo(rs.getBoolean("attivo"));
        prodotto.setIdCollezione(rs.getInt("id_collezione"));
        prodotto.setTagliaSelezionata(rs.getString("taglia"));
        return prodotto;
    }
}