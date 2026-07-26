package filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import model.bean.CategoriaBean;
import model.dao.CategoriaDAO;
import model.dao.impl.CategoriaDAOImpl;

@WebFilter("/*")
public class CategorieFilter implements Filter {

    private CategoriaDAO categoriaDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // Carica le categorie solo per richieste GET (pagine HTML), non per POST AJAX
        if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
            try {
                List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                request.setAttribute("categorie", categorie);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
