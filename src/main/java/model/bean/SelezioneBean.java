package model.bean;

import java.io.Serializable;

public class SelezioneBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idAcquisto;
    private int idCollezione;
    private int quantitaAcquistata;

    
    // Costruttore vuoto
    public SelezioneBean() {}

    
    // Getter e Setter
    public int getIdAcquisto() { 
    	return idAcquisto; 
    }
    public void setIdAcquisto(int idAcquisto) { 
    	this.idAcquisto = idAcquisto; 
    }

    
    public int getIdCollezione() { 
    	return idCollezione; 
    }
    public void setIdCollezione(int idCollezione) { 
    	this.idCollezione = idCollezione; 
    }

    
    public int getQuantitaAcquistata() { 
    	return quantitaAcquistata; 
    }
    public void setQuantitaAcquistata(int quantitaAcquistata) { 
    	this.quantitaAcquistata = quantitaAcquistata; 
    }

    
    @Override
    public String toString() {
        return "Selezione{" +
                "idAcquisto=" + idAcquisto +
                ", idCollezione=" + idCollezione +
                ", quantitaAcquistata=" + quantitaAcquistata +
                '}';
    }
}