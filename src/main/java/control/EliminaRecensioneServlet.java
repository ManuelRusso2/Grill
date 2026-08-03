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
import model.dao.RecensioneDAO;
import model.dao.impl.RecensioneDAOImpl;

@WebServlet("/EliminaRecensioneServlet")
public class EliminaRecensioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RecensioneDAO recensioneDAO;

    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Sicurezza: Solo gli utenti loggati e ADMIN possono eliminare
        if (utente == null || !utente.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN); // Errore 403
            return;
        }

        String idRecensioneParam = request.getParameter("idRecensione");
        String idProdottoParam = request.getParameter("idProdotto");

        if (idRecensioneParam != null && idProdottoParam != null) {
            try {
                int idRecensione = Integer.parseInt(idRecensioneParam);
                int idProdotto = Integer.parseInt(idProdottoParam);

                recensioneDAO.doDelete(idRecensione);

                // Reindirizza alla pagina del dettaglio prodotto
                response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                return;
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
    }
}