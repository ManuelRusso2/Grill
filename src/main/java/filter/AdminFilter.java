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
 * AdminFilter
 * Filtro di sicurezza e controllo accessi dedicato all'Area Amministrativa dell'applicazione.
 * Intercetta tutte le richieste dirette ai percorsi protetti e alle Servlet riservate agli amministratori:
 *   {@code /admin/*} e {@code /jsp/admin/*} (pagine e viste admin)
 *   {@code /AdminProdottoServlet}, {@code /AdminOrdiniServlet}, 
 *   {@code /AdminCategoriaServlet} e {@code /AdminRecensioniServlet} (controller amministrativi)
 * 
 * Garantisce che soltanto gli utenti autenticati con ruolo/flag {@code isAdmin == true} possano proseguire la navigazione,
 * bloccando gli accessi non autorizzati e prevenendo il caching dei dati sensibili nel browser.
 */
@WebFilter(urlPatterns = {
    "/admin/*", 
    "/jsp/admin/*", 
    "/AdminProdottoServlet", 
    "/AdminOrdiniServlet",
    "/AdminCategoriaServlet",
    "/AdminRecensioniServlet"
})
public class AdminFilter implements Filter {

    /**
     * Inizializza il filtro. Viene eseguito una sola volta dal Servlet Container
     * durante la fase di caricamento e startup del filtro.
     * 
     * @param filterConfig Configurazione del filtro fornita dal contenitore Servlet
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Eventuale inizializzazione di risorse o parametri del filtro
    }

    /**
     * Intercetta ogni richiesta diretta agli URL pattern associati al filtro per verificare i permessi dell'utente.
     * 
     * Effettua i seguenti controlli sequenziali:
     *   Verifica la presenza di una sessione attiva e di un oggetto {@link UtenteBean} autenticato.
     *   Verifica che l'utente loggato possieda i privilegi da amministratore ({@code utente.isAdmin()}).
     *   Imposta le intestazioni HTTP anti-caching per impedire il recupero delle pagine riservate dalla cronologia.
     * 
     * @param request  L'oggetto {@link ServletRequest} generico (convertito internamente in {@link HttpServletRequest})
     * @param response L'oggetto {@link ServletResponse} generico (convertito internamente in {@link HttpServletResponse})
     * @param chain    La catena dei filtri {@link FilterChain} per inoltrare la richiesta al filtro/servlet successivo
     * @throws IOException      Se si verifica un errore di I/O durante il redirect o la risposta di errore
     * @throws ServletException Se si verifica un errore a livello di Servlet/Filter
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // =========================================================================
        // 1. CASTING DEGLI OGGETTI DI RICHIESTA E RISPOSTA IN AMBITO HTTP
        // =========================================================================
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // =========================================================================
        // 2. RECUPERO DELLA SESSIONE E DELL'UTENTE AUTENTICATO
        // getSession(false) evita la creazione involontaria di una nuova sessione se non esiste
        // =========================================================================
        HttpSession session = httpRequest.getSession(false);
        UtenteBean utente = null;
        
        if (session != null) {
            utente = (UtenteBean) session.getAttribute("utente");
        }
        
        // =========================================================================
        // 3. CONTROLLO 1: UTENTE NON AUTENTICATO (GUEST)
        // Se non esiste una sessione o l'utente non è loggato, viene reindirizzato al login
        // =========================================================================
        if (utente == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/jsp/common/login.jsp");
            return; // Interrompe l'esecuzione del filtro ed evita il proseguimento nella catena
        }
        
        // =========================================================================
        // 4. CONTROLLO 2: UTENTE AUTENTICATO MA PRIVO DI PRIVILEGI ADMIN (CLIENTE STANDARD)
        // Restituisce un codice di errore HTTP 403 (Forbidden)
        // =========================================================================
        if (!utente.isAdmin()) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: richiesti privilegi amministrativi.");
            return; // Interrompe il passaggio della richiesta
        }

        // =========================================================================
        // 5. APPLICAZIONE HEADER ANTI-CACHING
        // Impedisce al browser di salvare le pagine amministrative sensibili nella memoria cache,
        // evitando il ripristino dei dati protetti tramite il tasto "Indietro" dopo il logout.
        // =========================================================================
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpResponse.setHeader("Pragma", "no-cache");                                   // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0);                                       // Proxies

        // =========================================================================
        // 6. PROSEGUIMENTO NELLA CATENA (CHAIN)
        // Autorizzazione confermata: la richiesta viene passata al prossimo filtro o alla Servlet/JSP target
        // =========================================================================
        chain.doFilter(request, response);
    }

    /**
     * Pulisce le risorse allocate dal filtro prima che il Servlet Container lo dismetta.
     * Viene invocato alla chiusura dell'applicazione o al re-deploy del contesto.
     */
    @Override
    public void destroy() {
        // Eventuale rilascio di risorse terminate
    }
}