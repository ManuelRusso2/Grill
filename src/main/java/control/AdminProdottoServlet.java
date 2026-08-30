package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.dao.CategoriaDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

/**
 * Servlet per la gestione del catalogo prodotti nell'area amministrativa.
 * Gestisce la visualizzazione del catalogo, l'apertura dei form di creazione e modifica,
 * il salvataggio, l'aggiornamento e la disattivazione/eliminazione dei prodotti.
 */
@WebServlet("/AdminProdottoServlet")
public class AdminProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti ai Data Access Object per l'interazione con il database
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializzazione della Servlet: istanzia i DAO necessari per accedere alle tabelle
     * dei prodotti e delle categorie nel Database.
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Gestisce le richieste di lettura HTTP GET:
     * - Apertura del form per un nuovo prodotto (action="new")
     * - Apertura del form di modifica per un prodotto esistente (action="edit")
     * - Visualizzazione della lista generale dei prodotti gestiti dall'admin (default)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupera la sessione corrente per estrarre eventuali messaggi temporanei (Flash Attributes)
        HttpSession session = request.getSession(false);
        if (session != null) {
            moveSessionAttributeToRequest(session, request, "successMessage");
            moveSessionAttributeToRequest(session, request, "errorMessage");
        }

        // Lettura dell'azione richiesta tramite parametro query string
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                
                // Caso 1: Apertura del form per l'inserimento di un NUOVO prodotto
                case "new":
                    // Carica le categorie per popolare la selezione multipla nel form JSP
                    request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
                    request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                    return;

                // Caso 2: Apertura del form per la MODIFICA di un prodotto esistente
                case "edit":
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        int idProdotto = Integer.parseInt(idParam.trim());
                        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

                        // Se il prodotto esiste, carica i suoi dati e le categorie, poi inoltra alla vista
                        if (prodotto != null) {
                            request.setAttribute("prodotto", prodotto);
                            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
                            request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                            return;
                        } else if (session != null) {
                            session.setAttribute("errorMessage", "Prodotto richiesto non trovato.");
                        }
                    }
                    break;

                default:
                    break;
            }

            // Vista predefinita: Carica il listino prodotti per la tabella admin e le categorie
            request.setAttribute("prodottiAdmin", prodottoDAO.doRetrieveAllAdmin());
            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());

            // Reindirizza alla dashboard principale di gestione prodotti
            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Gestione errore in caso di ID prodotto non numerico o malformato
            if (session != null) session.setAttribute("errorMessage", "ID prodotto non valido.");
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        } catch (SQLException e) {
            // Gestione errore interno di accesso al database
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste di modifica/scrittura HTTP POST:
     * - Inserimento di un nuovo prodotto (action="save")
     * - Aggiornamento di un prodotto (action="update")
     * - Eliminazione/Disattivazione di un prodotto (action="delete")
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                
                // Salvataggio nel Database del nuovo prodotto
                case "save":
                    ProdottoBean nuovoProdotto = leggiProdottoDaRequest(request, false);
                    prodottoDAO.doSave(nuovoProdotto);
                    session.setAttribute("successMessage", "Prodotto \"" + nuovoProdotto.getNome() + "\" inserito con successo!");
                    break;

                // Aggiornamento dei campi di un prodotto esistente
                case "update":
                    ProdottoBean prodottoAggiornato = leggiProdottoDaRequest(request, true);
                    prodottoDAO.doUpdate(prodottoAggiornato);
                    session.setAttribute("successMessage", "Prodotto ID #" + prodottoAggiornato.getIdProdotto() + " aggiornato con successo!");
                    break;

                // Eliminazione o disattivazione logica del prodotto
                case "delete":
                    int idProdotto = Integer.parseInt(request.getParameter("id"));
                    prodottoDAO.doDelete(idProdotto);
                    session.setAttribute("successMessage", "Prodotto ID #" + idProdotto + " disattivato/eliminato con successo.");
                    break;

                default:
                    break;
            }

        } catch (NumberFormatException e) {
            // Intercetta errori nella formattazione dei numeri inseriti dall'utente nei campi input
            session.setAttribute("errorMessage", "Errore nei dati inseriti: verifica che prezzo, IVA e quantità siano numeri validi.");
        } catch (SQLException e) {
            // Intercetta violazioni dei vincoli di integrità o fallimenti SQL
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di persistenza nel Database: " + e.getMessage());
        }

        // Reindirizzamento secondo pattern PRG (Post-Redirect-Get) per evitare duplicazioni da F5
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        }
    }

    /**
     * Trasferisce un attributo temporaneo dalla Sessione alla Request (Flash Attribute)
     * e lo elimina subito dopo dalla sessione per evitare che persista nelle chiamate successive.
     */
    private void moveSessionAttributeToRequest(HttpSession session, HttpServletRequest request, String attributeName) {
        if (session.getAttribute(attributeName) != null) {
            request.setAttribute(attributeName, session.getAttribute(attributeName));
            session.removeAttribute(attributeName);
        }
    }

    /**
     * Mappa ed estrae tutti i parametri inviati nel form della Request HTTP per costruire
     * un oggetto ProdottoBean valorizzato.
     * 
     * @param request - La richiesta HTTP contenente i dati inviati dal form
     * @param conId   - Impostato su true per le operazioni di modifica (update), false per nuovi inserimenti
     * @return ProdottoBean compilato
     * @throws NumberFormatException - Inserito in caso di parsing errato di numeri (prezzo, quantità, ID)
     */
    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) throws NumberFormatException {
        ProdottoBean prodotto = new ProdottoBean();

        // Se l'operazione richiede l'ID (es. update), lo estrae dalla richiesta
        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        // Estrazione dati di base
        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        
        // Estrazione dell'aliquota IVA con valore di default al 22% in caso di campo vuoto
        String ivaParam = request.getParameter("iva");
        if (ivaParam != null && !ivaParam.trim().isEmpty()) {
            prodotto.setIva(Double.parseDouble(ivaParam.trim()));
        } else {
            prodotto.setIva(22.0); // Valore di default IVA standard
        }

        // Estrazione di giacenza di magazzino e taglie disponibili
        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTaglie(request.getParameter("taglie"));

        // Gestione dello stato "attivo"
        String attivoParam = request.getParameter("attivo");
        prodotto.setAttivo("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam));

        // Gestione opzionale dell'ID collezione
        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        // Estrazione delle categorie selezionate dal form (gestione checkbox/select multiple)
        String[] idCategorie = request.getParameterValues("idCategoria");
        if (idCategorie != null) {
            List<CategoriaBean> categorie = new ArrayList<>();
            for (String idCat : idCategorie) {
                CategoriaBean cat = new CategoriaBean();
                cat.setIdCategoria(Integer.parseInt(idCat));
                categorie.add(cat);
            }
            prodotto.setCategorie(categorie);
        }

        // Estrazione del percorso dell'immagine con rimozione degli slash iniziali e immagine predefinita di fallback
        String immagine = request.getParameter("immagine");
        if (immagine != null && !immagine.trim().isEmpty()) {
            prodotto.setImmagine(immagine.trim().replaceAll("^/+", ""));
        } else {
            prodotto.setImmagine("images/default.jpg");
        }

        return prodotto;
    }
}