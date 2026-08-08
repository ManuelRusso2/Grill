package control;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import model.dao.impl.ProdottoDAOImpl;


@WebServlet("/api/prodotti")
public class ProdottiApiServlet extends HttpServlet {
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
        
        PrintWriter out = response.getWriter();
        try {
            List<ProdottoBean> prodotti = prodottoDAO.doRetrieveAllClientiRaggruppati();
            
            // Mescola i prodotti casualmente
            Collections.shuffle(prodotti);
            
            // Limita a 10 prodotti
            if (prodotti.size() > 10) {
                prodotti = prodotti.subList(0, 10);
            }
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < prodotti.size(); i++) {
                ProdottoBean p = prodotti.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                   .append("\"idProdotto\":").append(p.getIdProdotto()).append(",")
                   .append("\"nome\":\"").append(p.getNome().replace("\\", "\\\\").replace("\"", "\\\"")).append("\",")
                   .append("\"costo\":").append(p.getCosto()).append(",")
                   .append("\"immagine\":\"").append(p.getImmagine()).append("\"")
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
