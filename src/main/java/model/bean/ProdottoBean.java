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
    private boolean attivo;
    private Integer idCollezione;
    private List<CategoriaBean> categorie;
    private String immagine;

    public ProdottoBean() {}

    public int getIdProdotto() { return idProdotto; }
    public void setIdProdotto(int idProdotto) { this.idProdotto = idProdotto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return idProdotto == ((ProdottoBean) obj).idProdotto;
    }

    @Override
    public int hashCode() {
        return 31 + idProdotto;
    }

    @Override
    public String toString() {
        return "Prodotto{idProdotto=" + idProdotto +
                ", nome='" + nome + '\'' +
                ", costo=" + costo +
                ", quantita=" + quantita +
                ", attivo=" + attivo +
                ", idCollezione=" + idCollezione +
                ", immagine='" + immagine + '\'' +
                ", categorie=" + categorie + '}';
    }
}
