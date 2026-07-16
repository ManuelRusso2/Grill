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
        // Inizializziamo il DAO per l'utilizzo all'interno della servlet
        this.utenteDAO = new UtenteDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Se un utente prova ad accedere a questa servlet in GET, lo reindirizziamo al form di login
        response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recuperiamo i parametri inviati dal form HTML/JSP
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Validazione base lato server per sicurezza extra
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email e password sono obbligatorie.");
            request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            return;
        }

        try {
            // 2. Chiamiamo il DAO per verificare le credenziali nel Database
            UtenteBean utente = utenteDAO.doRetrieveByLogin(email, password);

            if (utente != null) {
                // 3. Login con successo: Creiamo o recuperiamo la Sessione HTTP
                HttpSession session = request.getSession(true);
                
                // Salviamo l'intero oggetto utente nella sessione
                session.setAttribute("utente", utente);

                // 4. Controllo del Ruolo per il Reindirizzamento
                if (utente.isAdmin()) {
                    // Se è un amministratore, lo mandiamo alla dashboard admin
                    response.sendRedirect(request.getContextPath() + "/AdminOrdiniServlet");
                } else {
                    // Se è un cliente normale, lo mandiamo alla homepage o al catalogo
                    response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
                }
                
            } else {
                // Credenziali errate: impostiamo un messaggio di errore inline e torniamo alla pagina di login
                request.setAttribute("errorMessage", "Email o password errate. Riprova.");
                request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Lanciamo un errore 500 che verrà catturato automaticamente dalla pagina error500.jsp configurata nel web.xml
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}