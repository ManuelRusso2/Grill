package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.UtenteBean;

/**
 * UserFilter
 * Filtro di sicurezza preposto alla protezione dell'area riservata agli utenti autenticati.
 * 
 * Intercetta le richieste dirette alle risorse sensibili dell'applicazione:
 *   Servlet utente: {@code /CarrelloServlet}, {@code /CheckoutServlet}, {@code /ProfiloServlet}, {@code /DettaglioOrdineServlet}, {@code /FatturaServlet}
 *   Viste riservate JSP: {@code /jsp/user/carrello.jsp}, {@code /jsp/user/checkout.jsp}, {@code /jsp/user/profilo.jsp}, {@code /jsp/user/ordine-confermato.jsp}
 * 
 * Se un utente non autenticato tenta di accedere a una di queste risorse:
 *   Chiamate standard (HTML/Browser): viene eseguito un reindirizzamento HTTP 302 alla pagina di login.
 *   Chiamate asincrone (AJAX): viene restituito un codice di stato HTTP 401 (Unauthorized) unitamente a un payload JSON contenente l'URL di redirect.
 * 
 * Inoltre, per tutti gli utenti autenticati, imposta gli opportuni header HTTP per prevenire il caching dei dati personali nel browser.
 */
@WebFilter(urlPatterns = {
    // Servlet che richiedono obbligatoriamente il login
    "/CarrelloServlet",
    "/CheckoutServlet", 
    "/ProfiloServlet", 
    "/DettaglioOrdineServlet",
    "/FatturaServlet",
    
    // Pagine JSP sensibili (registrazione.jsp rimane ad accesso libero)
    "/jsp/user/carrello.jsp",
    "/jsp/user/checkout.jsp",
    "/jsp/user/profilo.jsp",
    "/jsp/user/ordine-confermato.jsp"
})
public class UserFilter implements Filter {

    /**
     * Inizializza il filtro di sicurezza.
     * Metodo eseguito una sola volta dal Servlet Container all'avvio del ciclo di vita del filtro.
     * 
     * @param filterConfig La configurazione del filtro fornita dal Servlet Container
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Eventuale inizializzazione di risorse o parametri del filtro
    }

    /**
     * Intercetta la richiesta HTTP in ingresso e verifica lo stato di autenticazione dell'utente.
     * 
     * @param request  L'oggetto {@link ServletRequest} generico
     * @param response L'oggetto {@link ServletResponse} generico
     * @param chain    La catena dei filtri {@link FilterChain} per inoltrare la richiesta
     * @throws IOException      Se si verifica un errore di I/O durante il reindirizzamento o la scrittura della risposta
     * @throws ServletException Se si verifica un errore a livello di Servlet/Filter
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // =========================================================================
        // 1. CASTING DEGLI OGGETTI DI RICHIESTA E RISPOSTA ALL'AMBITO HTTP
        // =========================================================================
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // =========================================================================
        // 2. RECUPERO DELLA SESSIONE E VERIFICA DELL'UTENTE AUTENTICATO
        // getSession(false) evita la creazione accidentale di una nuova sessione
        // =========================================================================
        HttpSession session = httpRequest.getSession(false);
        UtenteBean utente = null;
        if (session != null) {
            utente = (UtenteBean) session.getAttribute("utente");
        }
        
        // =========================================================================
        // 3. GESTIONE DELL'UTENTE NON AUTENTICATO (UTENTE OSPITE / GUEST)
        // =========================================================================
        if (utente == null) {
            // Ispezione dell'header HTTP "X-Requested-With" per individuare chiamate AJAX
            boolean isAjax = "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"));
            
            if (isAjax) {
                // 3a. Risposta per chiamate asincrone (AJAX)
                // Restituisce il codice HTTP 401 Unauthorized e un JSON con l'URL per il login client-side
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write("{\"success\": false, \"redirect\": \"" 
                        + httpRequest.getContextPath() + "/jsp/common/login.jsp\"}");
            } else {
                // 3b. Risposta per chiamate sincrone tradizionali (Browser)
                // Esegue il reindirizzamento diretto (HTTP status 302) alla pagina di login
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/jsp/common/login.jsp");
            }
            // Interrompe il passaggio della richiesta lungo la catena di filtri
            return;
        }

        // =========================================================================
        // 4. CONFIGURAZIONE DELLE INTESTAZIONI ANTI-CACHING
        // Per gli utenti autenticati, impedisce la memorizzazione in cache di pagine con
        // dati personali (evita l'esposizione di informazioni riservate tramite il tasto "Indietro")
        // =========================================================================
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpResponse.setHeader("Pragma", "no-cache");                                   // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0);                                       // Proxies

        // =========================================================================
        // 5. PROSEGUIMENTO NELLA CATENA DI FILTRI
        // Autorizzazione verificata: la richiesta prosegue verso la risorsa di destinazione
        // =========================================================================
        chain.doFilter(request, response);
    }

    /**
     * Rilascia le risorse allocate dal filtro prima della dismissione da parte del Servlet Container.
     */
    @Override
    public void destroy() {
        // Eventuale pulizia delle risorse
    }
}