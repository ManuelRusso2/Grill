package control;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;


@WebServlet("/VerificaEmailServlet")
public class VerificaEmailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UtenteDAO utenteDAO;

    
    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Impostiamo il tipo di contenuto della risposta come JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        boolean exists = false;

        try {
            if (email != null && !email.trim().isEmpty()) {
                // 2. Interroghiamo il DAO
                UtenteBean utente = utenteDAO.doRetrieveByEmail(email.trim());
                if (utente != null) {
                    exists = true; // L'email è già presente nel database
                }
            }
            
            // 3. Costruiamo la risposta JSON
            PrintWriter out = response.getWriter();
            out.print("{\"exists\": " + exists + "}");
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