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

import model.bean.AcquistoBean;
import model.bean.OrdineBean;
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.OrdineDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.OrdineDAOImpl;

/**
 * Servlet che si occupa di mostrare i dettagli specifici di un singolo acquisto.
 * Viene utilizzata sia dal cliente nel suo storico ordini sia dall'amministratore
 * nella gestione complessiva degli ordini. Include controlli di autorizzazione per
 * garantire che ciascun utente acceda solo ai propri ordini (a meno che non sia admin).
 */
@WebServlet("/DettaglioOrdineServlet")
public class DettaglioOrdineServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Oggetti DAO per l'accesso ai dati nel database
    private AcquistoDAO acquistoDAO;
    private OrdineDAO ordineDAO;

    /**
     * Inizializza le istanze dei DAO necessarie per recuperare sia le informazioni 
     * generali dell'acquisto (testata) sia le singole righe che lo compongono.
     */
    @Override
    public void init() throws ServletException {
        this.acquistoDAO = new AcquistoDAOImpl();
        this.ordineDAO = new OrdineDAOImpl();
    }

    /**
     * Gestisce le richieste GET per recuperare e mostrare i dettagli dell'ordine specificato.
     * 
     * @param request  L'oggetto HttpServletRequest che contiene la richiesta del client
     * @param response L'oggetto HttpServletResponse per l'invio della risposta al client
     * @throws ServletException Se si verifica un errore durante il dispatch della richiesta
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verifica dell'autenticazione: solo gli utenti registrati possono accedere
        UtenteBean utente = getLoggedUser(request);
        if (utente == null) {
            // Se l'utente non è loggato, reindirizza alla pagina di login
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // 2. Recupero del parametro ID dell'acquisto dall'URL
        String idParam = getTrimmedParam(request, "id");
        if (idParam == null) {
            // Se il parametro ID manca, reindirizza al profilo/storico ordini utente
            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            return;
        }

        try {
            // Conversione del parametro ID in intero
            int idAcquisto = Integer.parseInt(idParam);

            // 3. Recupero della testata dell'acquisto dal database
            AcquistoBean acquisto = acquistoDAO.doRetrieveById(idAcquisto);

            // Se l'acquisto cercato non viene trovato nel DB, restituisce un errore HTTP 404 (Not Found)
            if (acquisto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 4. CONTROLLO DI SICUREZZA (AUTORIZZAZIONE):
            // Un utente può visualizzare il dettaglio dell'ordine SOLO se:
            // - È un amministratore (ha permessi globali)
            // - È l'effettivo proprietario dell'acquisto
            if (!utente.isAdmin() && acquisto.getIdUtente() != utente.getIdUtente()) {
                // Errore HTTP 403 Forbidden per tentativi di accesso non autorizzati
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 5. Recupero delle singole righe d'ordine associate all'acquisto
            List<OrdineBean> dettagli = ordineDAO.doRetrieveByAcquisto(idAcquisto);

            // 6. Preparazione dei dati nell'ambito della richiesta (Request scope)
            request.setAttribute("acquisto", acquisto);
            request.setAttribute("dettagliOrdine", dettagli);

            // Inoltro della richiesta alla pagina JSP di visualizzazione dettaglio
            request.getRequestDispatcher("/jsp/common/dettaglio-ordine.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Se il parametro ID nell'URL non è un numero intero valido -> HTTP 400 Bad Request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            // Log dell'eccezione SQL e riscontro di errore interno al server -> HTTP 500
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste POST reindirizzandole al metodo {@link #doGet}.
     * 
     * @param request  La richiesta HTTP
     * @param response La risposta HTTP
     * @throws ServletException Se si verifica un errore durante l'elaborazione
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // =========================================================================
    // METODI HELPER
    // =========================================================================

    /**
     * Recupera l'utente correntemente autenticato dalla sessione HTTP.
     * 
     * @param request La richiesta HTTP corrente
     * @return L'oggetto {@link UtenteBean} se presente in sessione, altrimenti {@code null}
     */
    private UtenteBean getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
    }

    /**
     * Estrae un parametro dalla richiesta HTTP, ne rimuove gli spazi bianchi iniziali e finali,
     * e ne verifica la validità.
     * 
     * @param request La richiesta HTTP
     * @param name    Il nome del parametro da recuperare
     * @return La stringa formattata e ripulita, oppure {@code null} se il parametro è assente o vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}