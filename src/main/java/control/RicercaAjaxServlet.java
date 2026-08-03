package control;

import java.io.IOException;
import java.io.PrintWriter;
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
        response.setHeader("Cache-Control", "no-store");
        
        String query = request.getParameter("query");
        
        if (query == null || query.trim().length() < 2) {
            // Se la stringa è vuota o troppo corta (meno di 2 caratteri), restituiamo un array JSON vuoto
            response.getWriter().print("[]");
            return;
        }

        PrintWriter out = response.getWriter();
        try {
            List<ProdottoBean> prodottiTrovati = prodottoDAO.doRetrieveBySearch(query.trim());
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < prodottiTrovati.size(); i++) {
                ProdottoBean p = prodottiTrovati.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                   .append("\"id\":").append(p.getIdProdotto()).append(",")
                   .append("\"nome\":\"").append(p.getNome().replace("\\", "\\\\").replace("\"", "\\\"")).append("\",")
                   .append("\"prezzo\":").append(p.getCosto())
                   .append("}");
            }
            json.append("]");
            
            out.print(json.toString());
            out.flush();
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}