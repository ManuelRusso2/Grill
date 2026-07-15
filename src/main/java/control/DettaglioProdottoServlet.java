package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import model.dao.impl.ProdottoDAOImpl;


@WebServlet("/DettaglioProdottoServlet")
public class DettaglioProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;

    
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        
        if (idParam == null || idParam.trim().isEmpty()) {
            // Se non viene passato un ID valido, rimandiamo al catalogo
            response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
            return;
        }

        try {
            int idProdotto = Integer.parseInt(idParam);
            ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
            
            // Sicurezza: Se il prodotto non esiste o non è attivo (e chi naviga non è admin), non mostrarlo
            if (prodotto == null || !prodotto.isAttivo()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND); // Errore 404
                return;
            }
            
            // Salviamo il prodotto trovato nei dettagli della richiesta
            request.setAttribute("prodotto", prodotto);
            
            // Inoltriamo alla pagina JSP dedicata
            request.getRequestDispatcher("/jsp/dettaglio-prodotto.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            // Se l'id non è un numero valido
            response.sendError(HttpServletResponse.SC_BAD_REQUEST); // Errore 400
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Errore 500
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}