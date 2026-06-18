package model.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class AcquistoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idAcquisto;
    private double prezzoUnitario;
    private double sconto;
    private Timestamp dataAcquisto;
    private double iva;
    private String metodoPagamento;
    private String emailUtente;

    
    // Costruttore vuoto
    public AcquistoBean() {}

    
    // Getter e Setter
    public int getIdAcquisto() { 
    	return idAcquisto; 
    }
    
    public void setIdAcquisto(int idAcquisto) { 
    	this.idAcquisto = idAcquisto; 
    }

    
    public double getPrezzoUnitario() { 
    	return prezzoUnitario; 
    }
    public void setPrezzoUnitario(double prezzoUnitario) { 
    	this.prezzoUnitario = prezzoUnitario; 
    }

    
    public double getSconto() { 
    	return sconto; 
    }
    public void setSconto(double sconto) { 
    	this.sconto = sconto; 
    }

    
    public Timestamp getDataAcquisto() { 
    	return dataAcquisto; 
    }
    public void setDataAcquisto(Timestamp dataAcquisto) { 
    	this.dataAcquisto = dataAcquisto; 
    }

    
    public double getIva() { 
    	return iva; 
    }
    public void setIva(double iva) { 
    	this.iva = iva; 
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

    
    @Override
    public String toString() {
        return "Acquisto{" +
                "idAcquisto=" + idAcquisto +
                ", prezzoUnitario=" + prezzoUnitario +
                ", sconto=" + sconto +
                ", dataAcquisto=" + dataAcquisto +
                ", iva=" + iva +
                ", metodoPagamento='" + metodoPagamento + '\'' +
                ", emailUtente='" + emailUtente + '\'' +
                '}';
    }
}