package model.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class UtenteBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int idUtente;
    private String email;
    private String nome;
    private String cognome;
    private boolean isAdmin;
    private String password;
    private String telefono;
    private Timestamp dataRegistrazione;

    
    // Costruttore vuoto
    public UtenteBean() {}

    
    // Getter e Setter
    public int getIdUtente() {
    	return idUtente; 
    }
    public void setIdUtente(int idUtente) {
    	this.idUtente = idUtente; 
    }
    
    public String getEmail() {
    	return email; 
    }
    public void setEmail(String email) {
    	this.email = email; 
    }

    
    public String getNome() { 
    	return nome; 
    }
    public void setNome(String nome) { 
    	this.nome = nome; 
    }

    
    public String getCognome() { 
    	return cognome; 
    }
    public void setCognome(String cognome) {
    	this.cognome = cognome; 
    }

    
    public boolean isAdmin() { 
    	return isAdmin; 
    }
    public void setAdmin(boolean isAdmin) { 
    	this.isAdmin = isAdmin; 
    }

    
    public String getPassword() { 
    	return password; 
    }
    public void setPassword(String password) { 
    	this.password = password; 
    }

    
    
    
    public String getTelefono() { 
    	return telefono; 
    }
    public void setTelefono(String telefono) { 
    	this.telefono = telefono; 
    }
    
    
    public Timestamp getDataRegistrazione() {
    	return dataRegistrazione;
    }
    public void setDataRegistrazione(Timestamp dataRegistrazione) {
    	this.dataRegistrazione = dataRegistrazione;
    }
    
    
    /**
     * Confronta due oggetti UtenteBean basandosi sull'idUtente numerico.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UtenteBean other = (UtenteBean) obj;
        return idUtente == other.idUtente;
    }

    /**
     * Genera l'hash numerico sfruttando l'idUtente per le strutture dati veloci (es. HashMap).
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + idUtente;
        return result;
    }
}