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
 * Gestisce l'aggiunta, la modifica della quantità, la rimozione e i controlli di disponibilità a magazzino.
 */
public class ContenutoDAOImpl implements ContenutoDAO {

    // --- QUERY SQL PREPARATE ---

    // Recupera la quantità di uno specifico prodotto (e taglia) già presente nel carrello
    private static final String SELECT_SINGOLO_PRODOTTO =
        "SELECT quantita FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // Inserimento di una nuova voce nel carrello
    private static final String INSERT_PRODOTTO_CARRELLO =
        "INSERT INTO contenuto (id_carrello, id_prodotto, quantita, taglia) VALUES (?, ?, ?, ?)";

    // Aggiornamento della quantità per un elemento presente nel carrello
    private static final String UPDATE_QUANTITA_CARRELLO =
        "UPDATE contenuto SET quantita = ? WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // Rimozione di uno specifico elemento (prodotto + taglia) dal carrello
    private static final String DELETE_PRODOTTO_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // Controllo della giacenza/stock disponibile in magazzino per il prodotto
    private static final String SELECT_QUANTITA_DISPONIBILE =
        "SELECT quantita FROM prodotto WHERE id_prodotto = ?";

    // Estrazione dei prodotti nel carrello in JOIN con la tabella prodotto per recuperare i dettagli completi
    private static final String SELECT_PRODOTTI_JOIN =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.quantita AS disponibilita, p.attivo, p.id_collezione, c.quantita AS quantita_carrello, c.taglia " +
        "FROM contenuto c JOIN prodotto p ON c.id_prodotto = p.id_prodotto WHERE c.id_carrello = ?";

    /**
     * Aggiunge un prodotto (con una determinata taglia) al carrello.
     * Se il prodotto è già presente nel carrello, la quantità viene incrementata previo controllo dello stock disponibile.
     * 
     * @param idCarrello ID del carrello dell'utente
     * @param idProdotto ID del prodotto da aggiungere
     * @param quantita   Quantità da aggiungere
     * @param taglia     Taglia selezionata
     * @throws SQLException In caso di errore SQL
     */
    @Override
    public void doAddProduct(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            
            // 1. Controlliamo se il prodotto (con quella specifica taglia) è già presente nel carrello
            int quantitaEsistente = 0;
            try (PreparedStatement psCheck = con.prepareStatement(SELECT_SINGOLO_PRODOTTO)) {
                psCheck.setInt(1, idCarrello);
                psCheck.setInt(2, idProdotto);
                psCheck.setString(3, taglia);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        quantitaEsistente = rs.getInt("quantita");
                    }
                }
            }

            // 2. Recupera la quantità massima disponibile a magazzino
            int quantitaDisponibile = getQuantitaDisponibile(con, idProdotto);

            // 3. Verifica dello stock: interrompe l'operazione se la quantità richiesta supera la disponibilità
            int quantitaTotale = quantitaEsistente + quantita;
            if (quantitaTotale > quantitaDisponibile) {
                return;
            }

            // 4. Aggiorna la riga esistente oppure inserisce un nuovo elemento
            if (quantitaEsistente > 0) {
                // Aggiornamento quantità accumulata
                try (PreparedStatement psUpdate = con.prepareStatement(UPDATE_QUANTITA_CARRELLO)) {
                    psUpdate.setInt(1, quantitaTotale);
                    psUpdate.setInt(2, idCarrello);
                    psUpdate.setInt(3, idProdotto);
                    psUpdate.setString(4, taglia);
                    psUpdate.executeUpdate();
                }
            } else {
                // Inserimento nuova voce
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
     * Aggiorna direttamente la quantità di un prodotto presente nel carrello con un nuovo valore specificato.
     * Effettua un controllo sulla giacenza in magazzino prima dell'aggiornamento.
     * 
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto
     * @param quantita   Nuova quantità desiderata
     * @param taglia     Taglia del prodotto
     * @throws SQLException In caso di errore SQL
     */
    @Override
    public void doUpdateQuantity(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            int quantitaDisponibile = getQuantitaDisponibile(con, idProdotto);

            // Se la nuova quantità supera lo stock disponibile, l'operazione viene annullata
            if (quantita > quantitaDisponibile) {
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
     * Rimuove uno specifico prodotto (e taglia) dal carrello.
     * 
     * @param idCarrello ID del carrello
     * @param idProdotto ID del prodotto da rimuovere
     * @param taglia     Taglia del prodotto da rimuovere
     * @throws SQLException In caso di errore durante l'eliminazione
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
     * Recupera la mappa di tutti i prodotti presenti in un carrello e le relative quantità.
     * 
     * @param idCarrello ID del carrello da esaminare
     * @return Una Map contenente gli oggetti ProdottoBean come chiave e la quantità nel carrello come valore
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
     * Metodo privato di supporto per verificare la quantità totale disponibile a magazzino per un dato prodotto.
     * 
     * @param con        La connessione SQL attiva
     * @param idProdotto L'ID del prodotto da verificare
     * @return La quantità disponibile a magazzino (0 se non trovato)
     * @throws SQLException In caso di errore nella lettura
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
     * Metodo helper privato che mappa la riga corrente del ResultSet (frutto della JOIN) 
     * in un oggetto ProdottoBean.
     * 
     * @param rs Il ResultSet posizionato sul record corrente
     * @return L'oggetto ProdottoBean popolato con i dettagli e la taglia selezionata
     * @throws SQLException In caso di errore di lettura dei campi
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