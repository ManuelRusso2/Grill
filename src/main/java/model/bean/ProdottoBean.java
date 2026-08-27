package model.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ProdottoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idProdotto;
    private String nome;
    private String descrizione;
    private double costo;
    private double iva = 22.0;
    private int quantita;
    private boolean attivo;
    private Integer idCollezione;
    private List<CategoriaBean> categorie;
    private String immagine;
    private String taglie; 
    private String tagliaSelezionata; 

    public ProdottoBean() {}

    public int getIdProdotto() { return idProdotto; }
    public void setIdProdotto(int idProdotto) { this.idProdotto = idProdotto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    public Integer getIdCollezione() { return idCollezione; }
    public void setIdCollezione(Integer idCollezione) { this.idCollezione = idCollezione; }

    public List<CategoriaBean> getCategorie() { return categorie; }
    public void setCategorie(List<CategoriaBean> categorie) { this.categorie = categorie; }

    public String getImmagine() { return immagine; }
    public void setImmagine(String immagine) { this.immagine = immagine; }

    public String getTaglie() { return taglie; }
    public void setTaglie(String taglie) { this.taglie = taglie; }

    public String getTagliaSelezionata() { return tagliaSelezionata; }
    public void setTagliaSelezionata(String tagliaSelezionata) { this.tagliaSelezionata = tagliaSelezionata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProdottoBean that = (ProdottoBean) obj;
        if (idProdotto != that.idProdotto) return false;
        return Objects.equals(tagliaSelezionata, that.tagliaSelezionata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProdotto, tagliaSelezionata);
    }
}