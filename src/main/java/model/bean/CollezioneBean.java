package model.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class CollezioneBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idCollezione;
    private String nomeCollezione;
    private String descrizione;
    private Timestamp dataCreazione;

    
    // Costruttore vuoto
    public CollezioneBean() {}

    
    // Getter e Setter
    public int getIdCollezione() { 
    	return idCollezione; 
    }
    public void setIdCollezione(int idCollezione) { 
    	this.idCollezione = idCollezione; 
    }

    
    public String getNomeCollezione() { 
    	return nomeCollezione; 
    }
    public void setNomeCollezione(String nomeCollezione) { 
    	this.nomeCollezione = nomeCollezione; 
    }

    
    public String getDescrizione() { 
    	return descrizione; 
    }
    public void setDescrizione(String descrizione) { 
    	this.descrizione = descrizione; 
    }

    
    public Timestamp getDataCreazione() { 
    	return dataCreazione; 
    }
    public void setDataCreazione(Timestamp dataCreazione) { 
    	this.dataCreazione = dataCreazione; 
    }

    
    @Override
    public String toString() {
        return "Collezione{" +
                "idCollezione=" + idCollezione +
                ", nomeCollezione='" + nomeCollezione + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", dataCreazione=" + dataCreazione +
                '}';
    }
}