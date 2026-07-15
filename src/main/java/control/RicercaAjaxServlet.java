package control;

import java.io.IOException;
import java.io.PrintWriter;
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


@WebServlet("/RicercaAjaxServlet")
public class RicercaAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;

    
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String query = request.getParameter("query");
        
        if (query == null || query.trim().length() < 2) {
            // Se la stringa è vuota o troppo corta (meno di 2 caratteri), restituiamo un array JSON vuoto
            response.getWriter().print("[]");
            return;
        }

        try {
            List<ProdottoBean> prodottiTrovati = prodottoDAO.doRetrieveBySearch(query.trim());
            
            // Costruiamo manualmente l'array JSON di oggetti
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < prodottiTrovati.size(); i++) {
                ProdottoBean p = prodottiTrovati.get(i);
                json.append("{");
                json.append("\"id\":").append(p.getIdProdotto()).append(",");
                // Usiamo i backslash per evitare che eventuali virgolette nel nome rompano il JSON
                json.append("\"nome\":\"").append(p.getNome().replace("\"", "\\\"")).append("\",");
                json.append("\"prezzo\":").append(p.getCosto());
                json.append("}");
                
                if (i < prodottiTrovati.size() - 1) {
                    json.append(","); // Virgola tra gli oggetti, tranne l'ultimo
                }
            }
            
            json.append("]");
            
            PrintWriter out = response.getWriter();
            out.print(json.toString());
            out.flush();
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}