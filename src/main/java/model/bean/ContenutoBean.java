package model.bean;

import java.io.Serializable;

public class ContenutoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idCarrello;
    private int idProdotto;
    private int quantita;

    
    // Costruttore vuoto
    public ContenutoBean() {}

    
    // Getter e Setter
    public int getIdCarrello() { 
    	return idCarrello; 
    }
    public void setIdCarrello(int idCarrello) { 
    	this.idCarrello = idCarrello; 
    }

    
    public int getIdProdotto() { 
    	return idProdotto; 
    }
    public void setIdProdotto(int idProdotto) { 
    	this.idProdotto = idProdotto; 
    }

    
    public int getQuantita() { 
    	return quantita; 
    }
    public void setQuantita(int quantita) { 
    	this.quantita = quantita; 
    }

    
    @Override
    public String toString() {
        return "Contenuto{" +
                "idCarrello=" + idCarrello +
                ", idProdotto=" + idProdotto +
                ", quantita=" + quantita +
                '}';
    }
}