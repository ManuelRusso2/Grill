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
	private int idUtente;

	
	private String nomeUtente;
    private String cognomeUtente;
    private String emailUtente;
    // Nome del prodotto recensito (utile quando la query include la join con la tabella prodotto)
    private String nomeProdotto;
    
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

    
    public int getIdUtente() {
    	return idUtente; 
    }
    public void setIdUtente(int idUtente) {
    	this.idUtente = idUtente; 
    }

    
    public String getNomeUtente() { 
    	return nomeUtente; 
    }
    public void setNomeUtente(String nomeUtente) { 
    	this.nomeUtente = nomeUtente; 
    }

    
    public String getCognomeUtente() { 
    	return cognomeUtente; 
    }
    public void setCognomeUtente(String cognomeUtente) { 
    	this.cognomeUtente = cognomeUtente; 
    }

    
    public String getEmailUtente() { 
    	return emailUtente; 
    }
    public void setEmailUtente(String emailUtente) { 
    	this.emailUtente = emailUtente; 
    }
    
    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }
}