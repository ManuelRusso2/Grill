package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.RecensioneDAO;
import model.dao.impl.RecensioneDAOImpl;

@WebServlet("/EliminaRecensioneUtenteServlet")
public class EliminaRecensioneUtenteServlet extends HttpServlet {
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

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        String idRecensioneParam = request.getParameter("idRecensione");
        String idProdottoParam = request.getParameter("idProdotto");

        if (idRecensioneParam != null) {
            try {
                int idRecensione = Integer.parseInt(idRecensioneParam);
                RecensioneBean rec = recensioneDAO.doRetrieveById(idRecensione);

                if (rec == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Consentiamo la cancellazione solo al proprietario o all'admin
                if (utente.isAdmin() || utente.getIdUtente() == rec.getIdUtente()) {
                    recensioneDAO.doDelete(idRecensione);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                // Se abbiamo il parametro prodotto, torniamo al dettaglio prodotto, altrimenti al profilo
                if (idProdottoParam != null) {
                    response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
                } else {
                    response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
                }
                return;
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
    }
}
