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
 * Servlet per la gestione della modifica delle recensioni.
 * Gestisce due flussi principali:
 *   GET: Recupera i dati della recensione e mostra il form di modifica (edit-recensione.jsp).
 *   POST: Valida i nuovi dati inviati e aggiorna la recensione nel database.
 */
@WebServlet("/ModificaRecensioneServlet")
public class ModificaRecensioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /** DAO per l'accesso e la manipolazione dei dati relativi alle recensioni. */
    private RecensioneDAO recensioneDAO;

    /**
     * Inizializza la servlet e le sue dipendenze (istanziamento del DAO).
     * 
     * @throws ServletException se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        // Inizializzazione del DAO per la gestione della persistenza delle recensioni
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET.
     * Mostra la pagina di modifica della recensione dopo aver eseguito i controlli
     * di autenticazione, validità dei parametri e autorizzazione dell'utente.
     * 
     * @param request  l'oggetto {@link HttpServletRequest} contenente la richiesta del client
     * @param response l'oggetto {@link HttpServletResponse} per inviare la risposta
     * @throws ServletException se si verifica un errore durante il forwarding
     * @throws IOException      se si verifica un errore di I/O durante il redirect o l'invio dell'errore
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---------------------------------------------------------------------
        // 1. Controllo Autenticazione Utente
        // ---------------------------------------------------------------------
        // Recupera la sessione corrente senza crearne una nuova se non esiste
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Se l'utente non è autenticato, reindirizza alla pagina di login
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // ---------------------------------------------------------------------
        // 2. Recupero e Validazione del Parametro "idRecensione"
        // ---------------------------------------------------------------------
        String idRecensioneParam = request.getParameter("idRecensione");
        
        // Se l'ID manca o è vuoto, reindirizza l'utente alla pagina del proprio profilo
        if (idRecensioneParam == null || idRecensioneParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            return;
        }

        try {
            // Conversione del parametro ID in intero
            int idRecensione = Integer.parseInt(idRecensioneParam);
            
            // Recupero della recensione dal database tramite DAO
            RecensioneBean rec = recensioneDAO.doRetrieveById(idRecensione);
            
            // Se la recensione non viene trovata nel DB, restituisce errore 404 (Not Found)
            if (rec == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Recensione non trovata.");
                return;
            }

            // ---------------------------------------------------------------------
            // 3. Controllo Autorizzazione
            // ---------------------------------------------------------------------
            // La modifica è consentita SOLO all'amministratore oppure all'autore della recensione
            if (!utente.isAdmin() && utente.getIdUtente() != rec.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare questa recensione.");
                return;
            }

            // ---------------------------------------------------------------------
            // 4. Preparazione Dati e Inoltro alla View (JSP)
            // ---------------------------------------------------------------------
            // Salva il bean della recensione nella request per renderlo disponibile alla JSP
            request.setAttribute("recensione", rec);
            
            // Effettua il forward verso la vista di modifica della recensione
            request.getRequestDispatcher("/jsp/user/edit-recensione.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Se l'ID recensione non è un numero intero valido, restituisce errore 400 (Bad Request)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID recensione non valido.");
        } catch (SQLException e) {
            // Log dell'eccezione SQL lato server e restituzione di errore 500 (Internal Server Error)
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero della recensione.");
        }
    }

    /**
     * Gestisce le richieste HTTP POST.
     * Riceve i dati modificati dal form, li valida e aggiorna il record nel database.
     * 
     * @param request  l'oggetto {@link HttpServletRequest} contenente i dati del form
     * @param response l'oggetto {@link HttpServletResponse} per inviare la risposta
     * @throws ServletException se si verifica un errore durante il forwarding
     * @throws IOException      se si verifica un errore di I/O durante il redirect o l'invio dell'errore
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---------------------------------------------------------------------
        // 1. Controllo Autenticazione Utente
        // ---------------------------------------------------------------------
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Se la sessione è scaduta o l'utente non è loggato, reindirizza al login
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // ---------------------------------------------------------------------
        // 2. Lettura dei Parametri Inviati dalla Form
        // ---------------------------------------------------------------------
        String idRecensioneParam = request.getParameter("idRecensione");
        String descrizione = request.getParameter("descrizione");
        String valutazioneParam = request.getParameter("valutazione");
        String idProdottoParam = request.getParameter("idProdotto"); // Parametro opzionale per il redirect di ritorno

        // Controllo presenza dei parametri obbligatori
        if (idRecensioneParam == null || descrizione == null || valutazioneParam == null) {
            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            return;
        }

        try {
            // Parse dei valori numerici ricevuti
            int idRecensione = Integer.parseInt(idRecensioneParam);
            double valutazione = Double.parseDouble(valutazioneParam);

            // Verifica esistenza della recensione nel DB
            RecensioneBean rec = recensioneDAO.doRetrieveById(idRecensione);
            if (rec == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Recensione non trovata.");
                return;
            }

            // ---------------------------------------------------------------------
            // 3. Controllo Autorizzazione
            // ---------------------------------------------------------------------
            // Verifica che l'utente loggato sia il proprietario o un admin
            if (!utente.isAdmin() && utente.getIdUtente() != rec.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare questa recensione.");
                return;
            }

            // ---------------------------------------------------------------------
            // 4. Validazione dei Dati Inseriti
            // ---------------------------------------------------------------------
            descrizione = descrizione.trim();
            
            // Controllo che il testo non sia vuoto e che la valutazione sia compresa nel range valido [1.0 - 5.0]
            if (descrizione.isEmpty() || valutazione < 1.0 || valutazione > 5.0) {
                // Imposta un messaggio di errore e ricarica il form con i dati precedenti
                request.setAttribute("errorMessage", "Inserisci una valutazione valida (1-5) e un testo per la recensione.");
                request.setAttribute("recensione", rec);
                request.getRequestDispatcher("/jsp/user/edit-recensione.jsp").forward(request, response);
                return;
            }

            // ---------------------------------------------------------------------
            // 5. Aggiornamento nel Database
            // ---------------------------------------------------------------------
            // Aggiorna le proprietà del bean con i nuovi valori
            rec.setDescrizione(descrizione);
            rec.setValutazione(valutazione);
            
            // Esegue l'update sul database
            recensioneDAO.doUpdate(rec);

            // ---------------------------------------------------------------------
            // 6. Reindirizzamento Contestuale
            // ---------------------------------------------------------------------
            // Se la richiesta conteneva l'ID del prodotto, torna alla pagina di dettaglio del prodotto,
            // altrimenti reindirizza alla pagina del profilo utente.
            if (idProdottoParam != null && !idProdottoParam.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdottoParam.trim());
            } else {
                response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            }

        } catch (NumberFormatException e) {
            // Se i dati numerici (ID o valutazione) non sono formattati correttamente
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato dei dati inseriti non valido.");
        } catch (SQLException e) {
            // Gestione dell'errore di persistenza
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento della recensione.");
        }
    }
}