package model.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class CarrelloBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idCarrello;
	private int idUtente;

    private Map<ProdottoBean, Integer> prodotti = new HashMap<>();
    
    
    // Costruttore vuoto
    public CarrelloBean() {}

    
    // Getter e Setter
    public int getIdCarrello() { 
    	return idCarrello; 
    }
    public void setIdCarrello(int idCarrello) { 
    	this.idCarrello = idCarrello; 
    }

    
    public int getIdUtente() {
    	return idUtente; 
    }
    public void setIdUtente(int idUtente) {
    	this.idUtente = idUtente; 
    }

    
    public Map<ProdottoBean, Integer> getProdotti() {
        return prodotti;
    }
    public void setProdotti(Map<ProdottoBean, Integer> prodotti) {
        this.prodotti = prodotti;
    }
}