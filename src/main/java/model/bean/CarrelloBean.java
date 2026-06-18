package model.bean;

import java.io.Serializable;

public class CarrelloBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idCarrello;
    private String emailUtente;

    
    // Costruttore vuoto
    public CarrelloBean() {}

    
    // Getter e Setter
    public int getIdCarrello() { 
    	return idCarrello; 
    }
    public void setIdCarrello(int idCarrello) { 
    	this.idCarrello = idCarrello; 
    }

    
    public String getEmailUtente() { 
    	return emailUtente; 
    }
    public void setEmailUtente(String emailUtente) { 
    	this.emailUtente = emailUtente; 
    }

    
    @Override
    public String toString() {
        return "Carrello{" +
                "idCarrello=" + idCarrello +
                ", emailUtente='" + emailUtente + '\'' +
                '}';
    }
}