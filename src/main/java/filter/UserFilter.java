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
 * Filtro di sicurezza per proteggere l'area utente (es. profilo, carrello, checkout).
 * Se un utente non è loggato, viene reindirizzato alla pagina di login.
 */
@WebFilter(urlPatterns = {
	    // Servlet che richiedono obbligatoriamente il login
	    "/CarrelloServlet",
	    "/CheckoutServlet", 
	    "/ProfiloServlet", 
	    "/DettaglioOrdineServlet",
	    "/FatturaServlet",
	    
	    // Pagine JSP sensibili (escludendo registrazione.jsp che rimane libera)
	    "/jsp/user/carrello.jsp",
	    "/jsp/user/checkout.jsp",
	    "/jsp/user/profilo.jsp",
	    "/jsp/user/ordine-confermato.jsp"
	})

public class UserFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Metodo di inizializzazione (può rimanere vuoto se non serve una configurazione iniziale)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Recuperiamo la sessione corrente, senza crearne una nuova se non esiste (false)
        HttpSession session = httpRequest.getSession(false);
        
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
        
        // Se l'utente non è loggato
        if (utente == null) {
            // Reindirizza alla pagina di login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/jsp/common/login.jsp");
        } else {
            // Se è loggato, permette alla richiesta di proseguire verso la destinazione originale
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Metodo di distruzione del filtro
    }
}