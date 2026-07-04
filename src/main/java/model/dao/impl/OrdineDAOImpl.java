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

public class OrdineDAOImpl implements OrdineDAO {

    private static final String INSERT_ORDINE =
        "INSERT INTO ordine (id_acquisto, id_prodotto, prezzo_unitario, iva, quantita_acquistata, stato_spedizione) VALUES (?, ?, ?, ?, ?, ?)";

    
    private static final String SELECT_BY_ACQUISTO =
        "SELECT id_acquisto, id_prodotto, prezzo_unitario, iva, quantita_acquistata, stato_spedizione FROM ordine WHERE id_acquisto = ?";

    
    private static final String UPDATE_STATO_SPEDIZIONE =
        "UPDATE ordine SET stato_spedizione = ? WHERE id_acquisto = ? AND id_prodotto = ?";

    
    @Override
    public void doSave(OrdineBean ordine) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ORDINE)) {

            ps.setInt(1, ordine.getIdAcquisto());
            ps.setInt(2, ordine.getIdProdotto());
            ps.setDouble(3, ordine.getPrezzoUnitario());
            ps.setDouble(4, ordine.getIva());
            ps.setInt(5, ordine.getQuantitaAcquistata());
            // Stato di default iniziale se non impostato nel bean
            ps.setString(6, ordine.getStatoSpedizione() != null ? ordine.getStatoSpedizione() : "In elaborazione");

            ps.executeUpdate();
        }
    }

    
    @Override
    public List<OrdineBean> doRetrieveByAcquisto(int idAcquisto) throws SQLException {
        List<OrdineBean> dettagli = new ArrayList<>();
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ACQUISTO)) {

            ps.setInt(1, idAcquisto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdineBean ordine = new OrdineBean();
                    ordine.setIdAcquisto(rs.getInt("id_acquisto"));
                    ordine.setIdProdotto(rs.getInt("id_prodotto"));
                    ordine.setPrezzoUnitario(rs.getDouble("prezzo_unitario"));
                    ordine.setIva(rs.getDouble("iva"));
                    ordine.setQuantitaAcquistata(rs.getInt("quantita_acquistata"));
                    ordine.setStatoSpedizione(rs.getString("stato_spedizione"));
                    dettagli.add(ordine);
                }
            }
        }
        return dettagli;
    }

    
    @Override
    public boolean doUpdateStatoSpedizione(int idAcquisto, int idProdotto, String nuovoStato) throws SQLException {
        try (Connection con = ConnessioneDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_STATO_SPEDIZIONE)) {

            ps.setString(1, nuovoStato);
            ps.setInt(2, idAcquisto);
            ps.setInt(3, idProdotto);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}