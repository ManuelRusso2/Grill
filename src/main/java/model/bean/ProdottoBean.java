package model.bean;

import java.io.Serializable;
import java.util.List;

public class ProdottoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idProdotto;
    private String nome;
    private String descrizione;
    private double costo;
    private int quantita;
    private String tipo;
    private boolean attivo;
    private Integer idCollezione;
    
    // Lista per gestire la relazione N a N con le Categorie
    private List<CategoriaBean> categorie;

    // Costruttore vuoto
    public ProdottoBean() {}

    // Getter e Setter
    public int getIdProdotto() { 
    	return idProdotto; 
    }
    public void setIdProdotto(int idProdotto) { 
    	this.idProdotto = idProdotto; 
    }

    
    public String getNome() { 
    	return nome; 
    }
    public void setNome(String nome) { 
    	this.nome = nome; 
    }

    
    public String getDescrizione() { 
    	return descrizione; 
    }
    public void setDescrizione(String descrizione) { 
    	this.descrizione = descrizione; 
    }

    
    public double getCosto() { 
    	return costo; 
    }
    public void setCosto(double costo) { 
    	this.costo = costo; 
    }

    
    public int getQuantita() { 
    	return quantita; 
    }
    public void setQuantita(int quantita) { 
    	this.quantita = quantita; 
    }

    
    public String getTipo() { 
    	return tipo; 
    }
    public void setTipo(String tipo) { 
    	this.tipo = tipo; 
    }

    
    public boolean isAttivo() { 
    	return attivo; 
    }
    public void setAttivo(boolean attivo) { 
    	this.attivo = attivo; 
    }
    
    
    public Integer getIdCollezione() { 
    	return idCollezione; 
    }
    public void setIdCollezione(Integer idCollezione) { 
    	this.idCollezione = idCollezione; 
    }

    
    public List<CategoriaBean> getCategorie() { 
    	return categorie; 
    }
    public void setCategorie(List<CategoriaBean> categorie) { 
    	this.categorie = categorie; 
    }
    
    
    // METODI DI UTILITÀ
    
    // Serve a stabilire se due oggetti di tipo ProdottoBean rappresentano lo stesso identico prodotto nel mondo reale
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ProdottoBean altro = (ProdottoBean) obj;
        
        return idProdotto == altro.idProdotto;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + idProdotto;
        return result;
    }
    
    
    @Override
    public String toString() {
        return "Prodotto{" +
                "idProdotto=" + idProdotto +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", costo=" + costo +
                ", quantita=" + quantita +
                ", tipo='" + tipo + '\'' +
                ", attivo='" + attivo + '\'' +
                ", idCollezione=" + idCollezione +
                ", categorie=" + categorie +
                '}';
    }
}