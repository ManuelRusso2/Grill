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
        response.sendRedirect(request.getContextPath() + "/jsp/user/registrazione.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recupero e trim parametri
        String nome     = request.getParameter("nome");
        String cognome  = request.getParameter("cognome");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        nome     = nome     != null ? nome.trim()     : "";
        cognome  = cognome  != null ? cognome.trim()  : "";
        email    = email    != null ? email.trim()    : "";
        password = password != null ? password.trim() : "";
        telefono = telefono != null ? telefono.trim() : "";

        // 2. Validazione campo per campo
        String emailRegex    = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";
        String telefonoRegex = "^[0-9+\\s\\-]{7,20}$";
        boolean hasErrors = false;

        if (nome.isEmpty()) {
            request.setAttribute("errNome", "Il nome è obbligatorio.");
            hasErrors = true;
        } else if (nome.length() < 2) {
            request.setAttribute("errNome", "Il nome deve contenere almeno 2 caratteri.");
            hasErrors = true;
        }

        if (cognome.isEmpty()) {
            request.setAttribute("errCognome", "Il cognome è obbligatorio.");
            hasErrors = true;
        } else if (cognome.length() < 2) {
            request.setAttribute("errCognome", "Il cognome deve contenere almeno 2 caratteri.");
            hasErrors = true;
        }

        if (email.isEmpty()) {
            request.setAttribute("errEmail", "L'email è obbligatoria.");
            hasErrors = true;
        } else if (!email.matches(emailRegex)) {
            request.setAttribute("errEmail", "Inserisci un indirizzo email valido (es. nome@dominio.it).");
            hasErrors = true;
        }

        if (password.isEmpty()) {
            request.setAttribute("errPassword", "La password è obbligatoria.");
            hasErrors = true;
        } else if (password.length() < 6) {
            request.setAttribute("errPassword", "La password deve contenere almeno 6 caratteri.");
            hasErrors = true;
        } else if (!password.matches(".*[A-Z].*")) {
            request.setAttribute("errPassword", "La password deve contenere almeno una lettera maiuscola.");
            hasErrors = true;
        } else if (!password.matches(".*[0-9].*")) {
            request.setAttribute("errPassword", "La password deve contenere almeno un numero.");
            hasErrors = true;
        }

        if (!telefono.isEmpty() && !telefono.matches(telefonoRegex)) {
            request.setAttribute("errTelefono", "Inserisci un numero di telefono valido (es. 333 1234567).");
            hasErrors = true;
        }

        if (hasErrors) {
            // Ripopola i campi per non costringere l'utente a riscrivere tutto
            request.setAttribute("formNome", nome);
            request.setAttribute("formCognome", cognome);
            request.setAttribute("formEmail", email);
            request.setAttribute("formTelefono", telefono);
            request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
            return;
        }

        try {
            // 3. Controllo email duplicata
            if (utenteDAO.doRetrieveByEmail(email) != null) {
                request.setAttribute("errEmail", "Questa email è già registrata.");
                request.setAttribute("formNome", nome);
                request.setAttribute("formCognome", cognome);
                request.setAttribute("formEmail", email);
                request.setAttribute("formTelefono", telefono);
                request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
                return;
            }

            // 4. Salvataggio
            UtenteBean nuovoUtente = new UtenteBean();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password);
            nuovoUtente.setTelefono(telefono.isEmpty() ? null : telefono);
            nuovoUtente.setAdmin(false);

            utenteDAO.doSave(nuovoUtente);

            // 5. Login automatico: salviamo l'utente in sessione
            HttpSession session = request.getSession(true);
            session.setAttribute("utente", nuovoUtente);

            response.sendRedirect(request.getContextPath() + "/CatalogoServlet");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
