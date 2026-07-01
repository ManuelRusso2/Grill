package model.bean;

import java.io.Serializable;

public class OrdineBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idAcquisto;
    private int idProdotto;
    private double prezzoUnitario;
    private double sconto;
    private double iva;
    private int quantitaAcquistata;
    private String statoSpedizione;
    
    // Costruttore vuoto
    public OrdineBean() {}

    
    //Getter e Setter
    public int getIdAcquisto() { 
    	return idAcquisto; 
    }
    public void setIdAcquisto(int idAcquisto) { 
    	this.idAcquisto = idAcquisto; 
    }

    
    public int getIdProdotto() { 
    	return idProdotto; 
    }
    public void setIdProdotto(int idProdotto) { 
    	this.idProdotto = idProdotto; 
    }

    
    public double getPrezzoUnitario() { 
    	return prezzoUnitario; 
    }
    public void setPrezzoUnitario(double prezzoUnitario) { 
    	this.prezzoUnitario = prezzoUnitario; 
    }

    
    public double getSconto() { 
    	return sconto; 
    }
    public void setSconto(double sconto) { 
    	this.sconto = sconto; 
    }

    
    public double getIva() { 
    	return iva; 
    }
    public void setIva(double iva) { 
    	this.iva = iva; 
    }

    
    public int getQuantitaAcquistata() { 
    	return quantitaAcquistata; 
    }
    public void setQuantitaAcquistata(int quantitaAcquistata) { 
    	this.quantitaAcquistata = quantitaAcquistata; 
    }
    
    
    public String getStatoSpedizione() { 
    	return statoSpedizione; 
    }
    public void setStatoSpedizione(String statoSpedizione) { 
    	this.statoSpedizione = statoSpedizione; 
    }

    
    @Override
    public String toString() {
        return "SelezioneBean{" +
                "idAcquisto=" + idAcquisto +
                ", idProdotto=" + idProdotto +
                ", prezzoUnitario=" + prezzoUnitario +
                ", sconto=" + sconto +
                ", iva=" + iva +
                ", quantitaAcquistata=" + quantitaAcquistata +
                ", statoSpedizione=" + statoSpedizione +
                '}';
    }
}