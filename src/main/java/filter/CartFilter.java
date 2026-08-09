package filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.bean.CarrelloBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;

@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class CartFilter implements Filter {

    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        
        int cartCount = 0;
        
        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");
            if (utente != null && !utente.isAdmin()) {
                try {
                    CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(utente.getIdUtente());
                    if (carrello != null) {
                        Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
                        if (prodottiInCarrello != null) {
                            for (Integer qty : prodottiInCarrello.values()) {
                                if (qty != null) {
                                    cartCount += qty;
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        httpRequest.setAttribute("cartCount", cartCount);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}