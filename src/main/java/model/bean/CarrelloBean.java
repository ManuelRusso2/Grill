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
    
    
    // METODI DI UTILITÀ PER IL CARRELLO

    // Aggiunge un prodotto o aumenta la quantità se c'è già
    public void addProdotto(ProdottoBean prodotto, int quantita) {
        if (this.prodotti.containsKey(prodotto)) {
            int vecchiaQuantita = this.prodotti.get(prodotto);
            this.prodotti.put(prodotto, vecchiaQuantita + quantita);
        } else {
            this.prodotti.put(prodotto, quantita);
        }
    }

    
    // Rimuove un prodotto dal carrello
    public void removeProdotto(ProdottoBean prodotto) {
        this.prodotti.remove(prodotto);
    }
    
    
    // Calcola il prezzo totale del carrello
    public double getPrezzoTotale() {
        double totale = 0;
        for (Map.Entry<ProdottoBean, Integer> el : prodotti.entrySet()) {
            totale += el.getKey().getCosto() * el.getValue();
        }
        return totale;
    }
    
    
    @Override
    public String toString() {
        return "CarrelloBean{" +
                "idCarrello=" + idCarrello +
                ", idUtente='" + idUtente + '\'' +
                ", prodotti=" + prodotti +
                '}';
    }
}