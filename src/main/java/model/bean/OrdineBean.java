package model.bean;

import java.io.Serializable;

public class OrdineBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idAcquisto;
    private int idProdotto;
    private double prezzoUnitario;
    private double iva;
    private int quantitaAcquistata;
    private String statoSpedizione;
    private String taglia;
    private String nomeProdotto;
    
    // Costruttore vuoto
    public OrdineBean() {}

    // Getter e Setter
    public int getIdAcquisto() { return idAcquisto; }
    public void setIdAcquisto(int idAcquisto) { this.idAcquisto = idAcquisto; }

    public int getIdProdotto() { return idProdotto; }
    public void setIdProdotto(int idProdotto) { this.idProdotto = idProdotto; }

    public double getPrezzoUnitario() { return prezzoUnitario; }
    public void setPrezzoUnitario(double prezzoUnitario) { this.prezzoUnitario = prezzoUnitario; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public int getQuantitaAcquistata() { return quantitaAcquistata; }
    public void setQuantitaAcquistata(int quantitaAcquistata) { this.quantitaAcquistata = quantitaAcquistata; }
    
    public String getStatoSpedizione() { return statoSpedizione; }
    public void setStatoSpedizione(String statoSpedizione) { this.statoSpedizione = statoSpedizione; }

    public String getTaglia() { return taglia; }
    public void setTaglia(String taglia) { this.taglia = taglia; }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }
}