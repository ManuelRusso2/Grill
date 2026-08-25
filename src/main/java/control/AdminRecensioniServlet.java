package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CategoriaBean;
import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.CategoriaDAO;
import model.dao.RecensioneDAO;
import model.dao.UtenteDAO;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.RecensioneDAOImpl;
import model.dao.impl.UtenteDAOImpl;

/**
 * Servlet per la gestione delle recensioni nell'area amministrativa.
 * Permette agli amministratori di visualizzare tutte le recensioni rilasciate dagli utenti,
 * filtrarle per un determinato cliente ed eventualmente eliminarle.
 */
@WebServlet("/AdminRecensioniServlet")
public class AdminRecensioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti ai DAO per la manipolazione di recensioni, utenti e categorie
    private RecensioneDAO recensioneDAO;
    private UtenteDAO utenteDAO;
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializzazione della Servlet: istanzia le implementazioni concrete dei DAO necessari.
     */
    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per la visualizzazione e il filtraggio delle recensioni.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Controllo di Sicurezza: verifica che la richiesta provenga da un utente con ruolo Amministratore
        if (isNotAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN); // Risponde con 403 Forbidden se non autorizzato
            return;
        }

        try {
            // 1. Carica i dati di supporto necessari per i menu di navigazione e i filtri di selezione (Categorie e Utenti)
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);

            List<UtenteBean> tuttiUtenti = utenteDAO.doRetrieveAllClienti();
            request.setAttribute("tuttiUtenti", tuttiUtenti);

            // 2. Recupera l'eventuale parametro di filtraggio dell'utente
            String idUtenteParam = getTrimmedParam(request, "idUtente");
            List<RecensioneBean> tutteRecensioni;

            // Se è stato specificato un ID utente, filtra le recensioni rilasciate solo da quello specifico cliente
            if (idUtenteParam != null) {
                int idUtente = Integer.parseInt(idUtenteParam);
                tutteRecensioni = recensioneDAO.doRetrieveByUtente(idUtente);
                request.setAttribute("utenteSelezionato", idUtente); // Mantiene il valore selezionato nel form della JSP
            } else {
                // Altrimenti carica la lista completa delle recensioni presenti nel sistema
                tutteRecensioni = recensioneDAO.doRetrieveAll();
            }

            // Inoltra l'elenco delle recensioni e reindirizza alla pagina di gestione
            request.setAttribute("tutteRecensioni", tutteRecensioni);
            request.getRequestDispatcher("/jsp/admin/gestione-recensioni.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Risponde con HTTP 400 Bad Request in caso di ID utente non numerico
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            // Log dell'errore di database e invio del codice HTTP 500 Internal Server Error
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste HTTP POST per l'esecuzione di azioni (es. eliminazione di una recensione).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Controllo di Sicurezza: garantisce che solo un amministratore possa eseguire operazioni di modifica/cancellazione
        if (isNotAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Estrae l'azione da eseguire e l'eventuale ID dell'utente per mantenere il filtro attivo dopo il reindirizzamento
        String action = getTrimmedParam(request, "action");
        String idUtenteParam = getTrimmedParam(request, "idUtente");
        String redirectUrl = request.getContextPath() + "/AdminRecensioniServlet";

        try {
            // Gestisce la cancellazione della recensione se l'azione richiesta è "delete"
            if ("delete".equalsIgnoreCase(action)) {
                String idRecensioneParam = getTrimmedParam(request, "idRecensione");
                if (idRecensioneParam != null) {
                    int idRecensione = Integer.parseInt(idRecensioneParam);
                    recensioneDAO.doDelete(idRecensione); // Rimuove la recensione dal database
                }
            }

            // Mantiene il filtro per l'utente selezionato nella vista ricostruendo l'URL di redirect
            if (idUtenteParam != null) {
                redirectUrl += "?idUtente=" + idUtenteParam;
            }

        } catch (NumberFormatException e) {
            // Memorizza un messaggio di errore in sessione se l'ID non è formattato correttamente
            request.getSession(true).setAttribute("errorMessage", "ID recensione non valido.");
        } catch (SQLException e) {
            // Memorizza un messaggio di errore in sessione in caso di fallimento della query SQL
            e.printStackTrace();
            request.getSession(true).setAttribute("errorMessage", "Errore durante l'eliminazione della recensione.");
        }

        // Applica il pattern Post/Redirect/Get reindirizzando alla vista aggiornata delle recensioni
        response.sendRedirect(redirectUrl);
    }

    // =========================================================================
    // HELPER METHODS (METODI DI SUPPORTO)
    // =========================================================================

    /**
     * Verifica se l'utente attualmente registrato in sessione non ha i permessi da Amministratore.
     * 
     * @param request la richiesta HTTP attiva
     * @return true se l'utente è nullo o non è un amministratore, false altrimenti
     */
    private boolean isNotAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
        return utente == null || !utente.isAdmin();
    }

    /**
     * Estrae un parametro dalla richiesta HTTP eseguendo la pulizia degli spazi bianchi (trim).
     * 
     * @param request la richiesta HTTP da cui leggere il parametro
     * @param name    il nome del parametro da estrarre
     * @return la stringa sanificata, oppure null se il parametro è assente o vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}