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
 * il salvataggio, l'aggiornamento e la disattivazione/eliminazione dei prodotti,
 * oltre alla gestione degli alert di sistema a livello di ServletContext.
 */
@WebServlet("/AdminProdottoServlet")
public class AdminProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti alle interfacce DAO per la gestione di prodotti e categorie
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializzazione della Servlet: istanzia le implementazioni concrete dei DAO necessari.
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per la navigazione e la consultazione del catalogo amministrativo.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupera la sessione corrente (se esiste) per estrarre eventuali messaggi di feedback da mostrare all'utente
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Sposta i messaggi dalla sessione alla Request e li rimuove dalla sessione (flash messages)
            moveSessionAttributeToRequest(session, request, "successMessage");
            moveSessionAttributeToRequest(session, request, "errorMessage");
        }

        // Recupera l'azione richiesta dall'URL (es. ?action=new o ?action=edit)
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            // Valuta l'azione in modalità lettura
            switch (action.toLowerCase()) {
                
                // Caso per la creazione di un nuovo prodotto: carica le categorie e inoltra al form vuoto
                case "new":
                    request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
                    request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                    return;

                // Caso per la modifica di un prodotto esistente
                case "edit":
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        int idProdotto = Integer.parseInt(idParam.trim());
                        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

                        // Se il prodotto viene trovato, imposta prodotto e categorie nella Request e apre il form
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

            // Vista predefinita: recupera la lista completa dei prodotti lato admin, le categorie e gestisce gli alert
            request.setAttribute("prodottiAdmin", prodottoDAO.doRetrieveAllAdmin());
            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
            processAdminAlerts(request);

            // Inoltra alla pagina JSP della tabella di gestione dei prodotti
            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Gestione dell'errore di conversione dell'ID numerico
            if (session != null) session.setAttribute("errorMessage", "ID prodotto non valido.");
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        } catch (SQLException e) {
            // Log dell'errore di database e invio del codice HTTP 500
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste HTTP POST per il salvataggio, l'aggiornamento e l'eliminazione dei prodotti.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ottiene o crea la sessione HTTP per memorizzare l'esito dell'operazione
        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                
                // Inserimento di un nuovo prodotto nel database
                case "save":
                    ProdottoBean nuovoProdotto = leggiProdottoDaRequest(request, false);
                    prodottoDAO.doSave(nuovoProdotto);
                    session.setAttribute("successMessage", "Prodotto \"" + nuovoProdotto.getNome() + "\" inserito con successo!");
                    break;

                // Aggiornamento dei dati di un prodotto esistente
                case "update":
                    ProdottoBean prodottoAggiornato = leggiProdottoDaRequest(request, true);
                    prodottoDAO.doUpdate(prodottoAggiornato);
                    session.setAttribute("successMessage", "Prodotto ID #" + prodottoAggiornato.getIdProdotto() + " aggiornato con successo!");
                    break;

                // Eliminazione/Disattivazione di un prodotto tramite ID
                case "delete":
                    int idProdotto = Integer.parseInt(request.getParameter("id"));
                    prodottoDAO.doDelete(idProdotto);
                    session.setAttribute("successMessage", "Prodotto ID #" + idProdotto + " disattivato/eliminato con successo.");
                    break;

                default:
                    break;
            }

        } catch (NumberFormatException e) {
            // Gestione degli errori nei tipi di dato inseriti nei campi numerici (es. prezzo o quantità)
            session.setAttribute("errorMessage", "Errore nei dati inseriti: verifica che prezzo e quantità siano numeri validi.");
        } catch (SQLException e) {
            // Gestione degli errori SQL a livello di persistenza
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di persistenza nel Database: " + e.getMessage());
        }

        // Se la risposta non è stata ancora inviata, reindirizza alla servlet per applicare il pattern Post/Redirect/Get
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        }
    }

    // =========================================================================
    // HELPER METHODS (METODI DI SUPPORTO)
    // =========================================================================

    /**
     * Trasferisce un attributo memorizzato temporaneamente in Sessione all'interno del Request Scope,
     * rimuovendolo dalla Sessione subito dopo (utile per i messaggi di notifica "flash").
     */
    private void moveSessionAttributeToRequest(HttpSession session, HttpServletRequest request, String attributeName) {
        if (session.getAttribute(attributeName) != null) {
            request.setAttribute(attributeName, session.getAttribute(attributeName));
            session.removeAttribute(attributeName);
        }
    }

    /**
     * Estrae in modo thread-safe gli alert di sistema memorizzati nel ServletContext (es. notifiche di stock in esaurimento),
     * li sposta nella Request per essere visualizzati nella JSP e svuota la lista originale nel contesto.
     */
    private void processAdminAlerts(HttpServletRequest request) {
        synchronized (getServletContext()) {
            Object rawAlerts = getServletContext().getAttribute("adminAlerts");
            if (rawAlerts instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> adminAlerts = (List<String>) rawAlerts;

                if (!adminAlerts.isEmpty()) {
                    // Crea una copia separata della lista per la request e svuota il buffer di alert nel contesto
                    request.setAttribute("adminAlerts", new ArrayList<>(adminAlerts));
                    adminAlerts.clear();
                }
            }
        }
    }

    /**
     * Mappa ed estrae i parametri inviati nel form della Request per costruire un oggetto {@link ProdottoBean}.
     * 
     * @param request la richiesta HTTP contenente i parametri del form
     * @param conId   se true estrae anche l'ID del prodotto (necessario per l'update), altrimenti lo ignora (per la save)
     * @return l'oggetto ProdottoBean popolato con i valori letti
     * @throws NumberFormatException se i campi numerici (ID, prezzo, quantità, collezione) non sono formattati correttamente
     */
    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) throws NumberFormatException {
        ProdottoBean prodotto = new ProdottoBean();

        // Estrazione ID prodotto in caso di aggiornamento
        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        // Estrazione dei dati testuali e numerici base
        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTaglie(request.getParameter("taglie"));

        // Conversione del flag 'attivo' da parametro stringa o checkbox ("true" / "on") a booleano
        String attivoParam = request.getParameter("attivo");
        prodotto.setAttivo("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam));

        // Gestione opzionale dell'ID Collezione
        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        // Estrazione delle categorie selezionate (gestisce selezioni multiple da form/checkbox)
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

        // Pulizia e normalizzazione del percorso dell'immagine allegata
        String immagine = request.getParameter("immagine");
        if (immagine != null && !immagine.trim().isEmpty()) {
            // Rimuove eventuali slash iniziali per mantenere relativo il percorso dell'immagine
            prodotto.setImmagine(immagine.trim().replaceAll("^/+", ""));
        } else {
            // Imposta un'immagine di fallback predefinita se il campo viene lasciato vuoto
            prodotto.setImmagine("images/default.jpg");
        }

        return prodotto;
    }
}