package model.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class AcquistoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idAcquisto;
    private double prezzoTotale;
    private Timestamp dataAcquisto;
    private String metodoPagamento;
    private String emailUtente;
    
    // La lista dei prodotti associati a questo acquisto
    private List<SelezioneBean> prodottiAcquistati = new ArrayList<>();
    
    // Costruttore vuoto
    public AcquistoBean() {}

    
    // Getter e Setter
    public int getIdAcquisto() { 
    	return idAcquisto; 
    }
    public void setIdAcquisto(int idAcquisto) { 
    	this.idAcquisto = idAcquisto; 
    }

    
    public double getPrezzoTotale() { 
    	return prezzoTotale; 
    }
    public void setPrezzoUnitario(double prezzoTotale) { 
    	this.prezzoTotale = prezzoTotale; 
    }

    
    public Timestamp getDataAcquisto() { 
    	return dataAcquisto; 
    }
    public void setDataAcquisto(Timestamp dataAcquisto) { 
    	this.dataAcquisto = dataAcquisto; 
    }

    
    public String getMetodoPagamento() { 
    	return metodoPagamento; 
    }
    public void setMetodoPagamento(String metodoPagamento) { 
    	this.metodoPagamento = metodoPagamento; 
    }

    
    public String getEmailUtente() { 
    	return emailUtente; 
    }
    public void setEmailUtente(String emailUtente) { 
    	this.emailUtente = emailUtente; 
    }

    
    public List<SelezioneBean> getProdottiAcquistati() { 
    	return prodottiAcquistati; 
    }
    public void setProdottiAcquistati(List<SelezioneBean> prodottiAcquistati) { 
    	this.prodottiAcquistati = prodottiAcquistati; 
    }
    
    
    // Metodo di utilità per aggiungere un prodotto alla volta
    public void addProdottoAcquistato(SelezioneBean item) { 
    	this.prodottiAcquistati.add(item); 
    }
    
    
    @Override
    public String toString() {
        return "Acquisto{" +
                "idAcquisto=" + idAcquisto +
                ", prezzoTotale=" + prezzoTotale +
                ", dataAcquisto=" + dataAcquisto +
                ", metodoPagamento='" + metodoPagamento + '\'' +
                ", emailUtente='" + emailUtente + '\'' +
                '}';
    }
}