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

// L'annotazione dice al server di attivare il filtro per qualsiasi richiesta che va verso /admin/ o verso le servlet admin
@WebFilter(urlPatterns = {"/admin/*", "/AdminProdottoServlet", "/AdminOrdiniServlet"})
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	
    }

    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 1. Prendiamo la sessione corrente senza crearne una nuova
        HttpSession session = httpRequest.getSession(false);
        
        boolean isAdmin = false;
        
        // 2. Controlliamo se esiste l'utente in sessione e se ha il ruolo di amministratore
        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");
            if (utente != null && utente.isAdmin() == true) {
                isAdmin = true;
            }
        }
        
        if (isAdmin) {
            // L'utente è autorizzato: la richiesta può proseguire verso la Servlet o la JSP richiesta
            chain.doFilter(request, response);
        } else {
            // L'utente non è admin o non è loggato: blocco e reindirizzamento (Errore 403 - Accesso Negato)
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN); // Invia il codice 403 gestito nel web.xml
        }
    }

    
    @Override
    public void destroy() {
 
    }
}