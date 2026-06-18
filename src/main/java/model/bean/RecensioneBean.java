package model.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class RecensioneBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idRecensione;
    private Timestamp dataRecensione;
    private String descrizione;
    private double valutazione;
    private int idProdotto;
    private String emailUtente;

    
    // Costruttore vuoto
    public RecensioneBean() {}

    
    // Getter e Setter
    public int getIdRecensione() { 
    	return idRecensione; 
    }
    public void setIdRecensione(int idRecensione) { 
    	this.idRecensione = idRecensione; 
    }

    
    public Timestamp getDataRecensione() { 
    	return dataRecensione; 
    }
    public void setDataRecensione(Timestamp dataRecensione) { 
    	this.dataRecensione = dataRecensione; 
    }

    
    public String getDescrizione() { 
    	return descrizione; 
    }
    public void setDescrizione(String descrizione) { 
    	this.descrizione = descrizione; 
    }

    
    public double getValutazione() { 
    	return valutazione; 
    }
    public void setValutazione(double valutazione) { 
    	this.valutazione = valutazione; 
    }

    
    public int getIdProdotto() { 
    	return idProdotto; 
    }
    public void setIdProdotto(int idProdotto) { 
    	this.idProdotto = idProdotto; 
    }

    
    public String getEmailUtente() { 
    	return emailUtente; 
    }
    public void setEmailUtente(String emailUtente) { 
    	this.emailUtente = emailUtente; 
    }

    
    @Override
    public String toString() {
        return "Recensione{" +
                "idRecensione=" + idRecensione +
                ", dataRecensione=" + dataRecensione +
                ", descrizione='" + descrizione + '\'' +
                ", valutazione=" + valutazione +
                ", idProdotto=" + idProdotto +
                ", emailUtente='" + emailUtente + '\'' +
                '}';
    }
}