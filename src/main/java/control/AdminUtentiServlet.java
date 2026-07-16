package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;

@WebServlet("/AdminUtentiServlet")
public class AdminUtentiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private UtenteDAO utenteDAO;

    @Override
    public void init() throws ServletException {
        // Inizializziamo il DAO per la gestione degli utenti
        this.utenteDAO = new UtenteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 1. Recuperiamo dal Database solo gli utenti con ruolo 'Cliente' (escludendo gli amministratori)
            List<UtenteBean> clienti = utenteDAO.doRetrieveAllClienti();
            
            // 2. Salviamo la lista nell'oggetto request per renderla accessibile alla JSP
            request.setAttribute("clienti", clienti);
            
            // 3. Inoltriamo la richiesta (Forward) alla pagina di gestione degli utenti nell'area admin
            request.getRequestDispatcher("/jsp/admin/gestione-utenti.jsp").forward(request, response);
            
        } catch (SQLException e) {
            // Log dell'eccezione SQL sulla console del server Tomcat
            e.printStackTrace();
            // Risposta con codice di errore 500 in caso di anomalie sul Database
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirigiamo eventuali chiamate in POST al metodo doGet
        doGet(request, response);
    }
}