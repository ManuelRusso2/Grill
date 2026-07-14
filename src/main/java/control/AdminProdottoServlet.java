package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import model.dao.impl.ProdottoDAOImpl;


@WebServlet("/AdminProdottoServlet")
public class AdminProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProdottoDAO prodottoDAO;

    
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // L'admin deve vedere TUTTO, anche i prodotti non attivi
            List<ProdottoBean> tuttiIProdotti = prodottoDAO.doRetrieveAllAdmin();
            
            request.setAttribute("prodottiAdmin", tuttiIProdotti);
            
            // Inoltriamo alla JSP nell'area riservata all'admin
            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Errore 500 gestito nel web.xml
        }
    }
}