package control;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * LogoutServlet
 * Servlet controller responsabile della disconnessione degli utenti (processo di Logout).
 * Svolge le seguenti operazioni fondamentali per la sicurezza:
 *   Invalida e distrugge la sessione HTTP corrente.
 *   Imposta gli header HTTP per disabilitare la cache del browser, impedendo la visualizzazione di contenuti protetti tramite il tasto "Indietro".
 *   Reindirizza l'utente alla vista pubblica principale (Catalogo).
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Gestisce le richieste HTTP GET per il processo di logout.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente la richiesta del client
     * @param response L'oggetto {@link HttpServletResponse} per inviare la risposta o reindirizzare il client
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore d'I/O durante il reindirizzamento
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. INVALIDAZIONE DELLA SESSIONE UTENTE
        // Richiede la sessione corrente senza instanziarne una nuova se non esiste (false)
        // =========================================================================
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Il metodo invalidate() rimuove tutti gli attributi salvati in sessione 
            // (es. "utente") e distrugge l'ID di sessione lato server.
            session.invalidate();
        }

        // =========================================================================
        // 2. SICUREZZA: DISABILITAZIONE CACHE DEL BROWSER
        // Configura gli header HTTP di risposta per impedire il salvataggio in cache.
        // Evita che un utente disconnesso possa visualizzare pagine protette premendo
        // il pulsante "Indietro" della cronologia del browser.
        // =========================================================================
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // Standard HTTP 1.1
        response.setHeader("Pragma", "no-cache");                                   // Retrocompatibilità HTTP 1.0
        response.setDateHeader("Expires", 0);                                       // Scadenza immediata per server Proxy

        // =========================================================================
        // 3. REINDIRIZZAMENTO
        // Utilizza il reindirizzamento client-side (HTTP 302) verso la Servlet del catalogo
        // =========================================================================
        response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
    }

    /**
     * Gestisce le richieste HTTP POST delegandole interamente al metodo {@link #doGet}.
     * Permette alla Servlet di supportare la disconnessione sia via link (GET) che via form (POST).
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore d'I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Inoltra la gestione del POST direttamente al metodo doGet per evitare duplicazione di codice
        doGet(request, response);
    }
}