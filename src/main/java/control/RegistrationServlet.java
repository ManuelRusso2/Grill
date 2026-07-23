package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;


@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UtenteDAO utenteDAO;

    
    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Se si accede in GET, mostriamo semplicemente la pagina del form
        response.sendRedirect(request.getContextPath() + "/jsp/user/registrazione.jsp");
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recupero parametri dal Form
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        nome = nome != null ? nome.trim() : null;
        cognome = cognome != null ? cognome.trim() : null;
        email = email != null ? email.trim() : null;
        password = password != null ? password.trim() : null;
        telefono = telefono != null ? telefono.trim() : null;

        // 2. Validazione Campi lato Server (Espressioni Regolari)
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (nome == null || nome.isEmpty() || 
            cognome == null || cognome.isEmpty() ||
            email == null || !email.matches(emailRegex) || 
            password == null || password.length() < 6) {
            
            request.setAttribute("errorMessage", "Dati inseriti non validi o non conformi.");
            request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
            return;
        }

        try {
            // 3. Controllo se l'email esiste già
            if (utenteDAO.doRetrieveByEmail(email) != null) {
                request.setAttribute("errorMessage", "Questa email è già registrata.");
                request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
                return;
            }

            // 4. Creazione del Bean e salvataggio (la password verrà cifrata nel DAO)
            UtenteBean nuovoUtente = new UtenteBean();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password); // Ci pensa UtenteDAOImpl a farne l'hash
            // username rimosso: non impostiamo più questo campo
            nuovoUtente.setTelefono(telefono);
            nuovoUtente.setAdmin(false); // Un utente che si registra da solo è sempre un cliente

            utenteDAO.doSave(nuovoUtente);

            // 5. Messaggio di successo e reindirizzamento al Login
            request.setAttribute("successMessage", "Registrazione completata con successo! Adesso puoi accedere.");
            request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Errore 500
        }
    }
}