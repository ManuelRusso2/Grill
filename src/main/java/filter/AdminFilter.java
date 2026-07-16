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
 * Filtro di sicurezza per l'Area Amministrativa.
 * Intercetta qualsiasi richiesta diretta alle cartelle protette o alle servlet dell'admin,
 * impedendo l'accesso ai non loggati o agli utenti senza privilegi amministrativi.
 */
@WebFilter(urlPatterns = {"/admin/*", "/jsp/admin/*", "/AdminProdottoServlet", "/AdminOrdiniServlet", "/AdminUtentiServlet"})
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Metodo di inizializzazione del filtro (eseguito una sola volta all'avvio del server)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Per poter usare i metodi specifici del protocollo HTTP (come sessioni e redirect),
        // dobbiamo fare il cast dei parametri generici ServletRequest e ServletResponse.
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 1. Recuperiamo la sessione corrente. 
        // Passando 'false' indichiamo di NON creare una nuova sessione se questa non esiste già.
        HttpSession session = httpRequest.getSession(false);
        
        boolean isAdmin = false;
        
        // 2. Controllo di sicurezza:
        // Verifichiamo che esista una sessione attiva, che ci sia un utente loggato 
        // e che l'attributo booleano 'isAdmin' nel Bean sia impostato su true.
        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");
            if (utente != null && utente.isAdmin()) { // utente.isAdmin() == true
                isAdmin = true;
            }
        }
        
        // 3. Gestione dei permessi
        if (isAdmin) {
            // Se l'utente è autorizzato, lasciamo proseguire la richiesta verso la sua destinazione originaria
            // invocando il filtro successivo nella catena.
            chain.doFilter(request, response);
        } else {
            // Se l'utente non è loggato o non è admin, blocchiamo l'accesso.
            // Inviamo un codice di stato HTTP 403 (Forbidden - Accesso Negato).
            // Questo errore verrà intercettato e mostrato graficamente tramite la pagina personalizzata nel web.xml.
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {
        // Metodo chiamato quando il filtro viene rimosso dal servizio
    }
}