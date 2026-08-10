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

public class ContenutoDAOImpl implements ContenutoDAO {

    // MODIFICATA: Aggiunto controllo sulla taglia
    private static final String SELECT_SINGOLO_PRODOTTO =
        "SELECT quantita FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // MODIFICATA: Aggiunta la colonna taglia
    private static final String INSERT_PRODOTTO_CARRELLO =
        "INSERT INTO contenuto (id_carrello, id_prodotto, quantita, taglia) VALUES (?, ?, ?, ?)";

    // MODIFICATA: Aggiunto controllo sulla taglia
    private static final String UPDATE_QUANTITA_CARRELLO =
        "UPDATE contenuto SET quantita = ? WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // MODIFICATA: Aggiunto controllo sulla taglia
    private static final String DELETE_PRODOTTO_CARRELLO =
        "DELETE FROM contenuto WHERE id_carrello = ? AND id_prodotto = ? AND taglia = ?";

    // MODIFICATA: Selezioniamo anche c.taglia
    private static final String SELECT_PRODOTTI_JOIN =
        "SELECT p.id_prodotto, p.nome, p.descrizione, p.costo, p.quantita AS disponibilita, p.attivo, p.id_collezione, c.quantita AS quantita_carrello, c.taglia " +
        "FROM contenuto c JOIN prodotto p ON c.id_prodotto = p.id_prodotto WHERE c.id_carrello = ?";

    @Override
    public void doAddProduct(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            // 1. Controlliamo se il prodotto (con quella specifica taglia) è già presente
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

            // 2. Recupera la quantità disponibile
            int quantitaDisponibile = 0;
            try (PreparedStatement psStock = con.prepareStatement(
                    "SELECT quantita FROM prodotto WHERE id_prodotto = ?")) {
                psStock.setInt(1, idProdotto);
                try (ResultSet rs = psStock.executeQuery()) {
                    if (rs.next()) {
                        quantitaDisponibile = rs.getInt("quantita");
                    }
                }
            }

            // 3. Verifica stock
            int quantitaTotale = quantitaEsistente + quantita;
            if (quantitaTotale > quantitaDisponibile) {
                return;
            }

            if (quantitaEsistente > 0) {
                // 4. Aggiorna
                try (PreparedStatement psUpdate = con.prepareStatement(UPDATE_QUANTITA_CARRELLO)) {
                    psUpdate.setInt(1, quantitaTotale);
                    psUpdate.setInt(2, idCarrello);
                    psUpdate.setInt(3, idProdotto);
                    psUpdate.setString(4, taglia);
                    psUpdate.executeUpdate();
                }
            } else {
                // 5. Inserisci nuovo
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

    @Override
    public void doUpdateQuantity(int idCarrello, int idProdotto, int quantita, String taglia) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection()) {
            int quantitaDisponibile = 0;
            try (PreparedStatement psStock = con.prepareStatement(
                    "SELECT quantita FROM prodotto WHERE id_prodotto = ?")) {
                psStock.setInt(1, idProdotto);
                try (ResultSet rs = psStock.executeQuery()) {
                    if (rs.next()) {
                        quantitaDisponibile = rs.getInt("quantita");
                    }
                }
            }

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

    @Override
    public Map<ProdottoBean, Integer> doRetrieveProdottiInCarrello(int idCarrello) throws SQLException {
        Map<ProdottoBean, Integer> mappaCarrello = new HashMap<>();
        
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_PRODOTTI_JOIN)) {

            ps.setInt(1, idCarrello);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProdottoBean prodotto = new ProdottoBean();
                    prodotto.setIdProdotto(rs.getInt("id_prodotto"));
                    prodotto.setNome(rs.getString("nome"));
                    prodotto.setDescrizione(rs.getString("descrizione"));
                    prodotto.setCosto(rs.getDouble("costo"));
                    prodotto.setQuantita(rs.getInt("disponibilita")); 
                    prodotto.setAttivo(rs.getBoolean("attivo"));
                    prodotto.setIdCollezione(rs.getInt("id_collezione"));
                    
                    // FONDAMENTALE: Associamo la taglia scelta al prodotto per riconoscerlo nel carrello
                    prodotto.setTagliaSelezionata(rs.getString("taglia")); 

                    int quantitaNelCarrello = rs.getInt("quantita_carrello");

                    mappaCarrello.put(prodotto, quantitaNelCarrello);
                }
            }
        }
        return mappaCarrello;
    }
}