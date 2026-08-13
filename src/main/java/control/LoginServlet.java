package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UtenteDAO utenteDAO;

    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email e password sono obbligatorie.");
            request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            return;
        }

        try {
            UtenteBean utente = utenteDAO.doRetrieveByLogin(email, password);

            if (utente != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("utente", utente);

                // Reindirizzamento unico alla Home Page per tutti i ruoli
                response.sendRedirect(request.getContextPath() + "/jsp/common/home.jsp");
                
            } else {
                request.setAttribute("errorMessage", "Email o password errate. Riprova.");
                request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}