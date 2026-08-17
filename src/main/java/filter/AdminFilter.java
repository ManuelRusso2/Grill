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
@WebFilter(urlPatterns = {"/admin/*", "/jsp/admin/*", "/AdminProdottoServlet", "/AdminOrdiniServlet"})
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Metodo di inizializzazione del filtro
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        boolean isAdmin = false;
        
        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");
            if (utente != null && utente.isAdmin()) {
                isAdmin = true;
            }
        }
        
        if (isAdmin) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {
        // Metodo di distruzione del filtro
    }
}