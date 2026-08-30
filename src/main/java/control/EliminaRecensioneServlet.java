package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.RecensioneDAO;
import model.dao.impl.RecensioneDAOImpl;

/**
 * Servlet unificata per la gestione dell'eliminazione delle recensioni.
 * Verifica i permessi di accesso garantendo che l'eliminazione possa essere
 * eseguita sia dall'autore originale della recensione sia da un amministratore.
 * Gestisce inoltre un reindirizzamento dinamico alla pagina del prodotto o al profilo utente.
 */
@WebServlet("/EliminaRecensioneServlet")
public class EliminaRecensioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // DAO per la manipolazione delle recensioni nel database
    private RecensioneDAO recensioneDAO;

    /**
     * Inizializza le istanze dei DAO necessari al funzionamento della Servlet.
     */
    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP POST per eliminare una recensione specificata.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente i parametri 'idRecensione' ed eventualmente 'idProdotto'
     * @param response L'oggetto {@link HttpServletResponse} per inviare risposte HTTP o eseguire redirect
     * @throws ServletException Se si verifica un errore nell'esecuzione della servlet
     * @throws IOException      Se si verifica un errore di I/O durante il redirect o la gestione della risposta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Controllo Autenticazione: verifica se l'utente è attualmente loggato in sessione
        UtenteBean utente = getLoggedUser(request);
        if (utente == null) {
            // Se l'utente non è loggato, reindirizza alla pagina di login
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // 2. Recupero e pulizia dei parametri inviati dalla richiesta
        String idRecensioneParam = getTrimmedParam(request, "idRecensione");
        String idProdottoParam = getTrimmedParam(request, "idProdotto");

        // L'ID della recensione è obbligatorio per identificare la risorsa da eliminare
        if (idRecensioneParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            return;
        }

        try {
            // Conversione dell'ID recensione in un intero
            int idRecensione = Integer.parseInt(idRecensioneParam);

            // 3. Recupero della recensione dal database per verificarne l'esistenza e la titolarità
            RecensioneBean recensione = recensioneDAO.doRetrieveById(idRecensione);

            // Se la recensione non viene trovata nel DB -> HTTP 404 Not Found
            if (recensione == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 4. Controllo Autorizzazione (Permessi):
            // L'operazione è consentita SOLO se l'utente è ADMIN oppure è il PROPRIETARIO della recensione
            if (!utente.isAdmin() && utente.getIdUtente() != recensione.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                return;
            }

            // 5. Cancellazione effettiva della recensione dal database
            recensioneDAO.doDelete(idRecensione);

            // 6. Reindirizzamento dinamico:
            // Se la cancellazione è stata inviata dalla scheda prodotto (idProdotto presente), torna lì;
            // altrimenti (es. inoltrata dallo storico recensioni personale), torna al profilo utente.
            if (idProdottoParam != null) {
                response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdottoParam);
            } else {
                response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            }

        } catch (NumberFormatException e) {
            // In caso di formato non numerico dell'ID -> HTTP 400 Bad Request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            // Log dell'errore database e risposta HTTP 500 Internal Server Error
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste HTTP GET rifiutandole con errore.
     * Le operazioni di eliminazione non devono mai essere esposte via GET per motivi di sicurezza.
     * 
     * @param request  La richiesta HTTP
     * @param response La risposta HTTP
     * @throws ServletException Se si verifica un errore interno
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // HTTP 405 Method Not Allowed per bloccare chiamate non autorizzate in GET
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Recupera l'utente autenticato memorizzato nell'oggetto sessione.
     * 
     * @param request La richiesta HTTP corrente
     * @return L'oggetto {@link UtenteBean} associato all'utente loggato, oppure {@code null} se assente
     */
    private UtenteBean getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (UtenteBean) session.getAttribute("utente");
        }

        return null;
    }

    /**
     * Estrae un parametro dalla richiesta HTTP, rimuove gli spazi bianchi
     * e restituisce {@code null} se risulta vuoto o non presente.
     * 
     * @param request La richiesta HTTP
     * @param name    Il nome del parametro da recuperare
     * @return Il valore del parametro pulito, oppure {@code null}
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return null;
    }
}