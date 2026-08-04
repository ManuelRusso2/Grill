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

@WebServlet("/ModificaRecensioneServlet")
public class ModificaRecensioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RecensioneDAO recensioneDAO;

    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        String idRecensioneParam = request.getParameter("idRecensione");
        if (idRecensioneParam == null) {
            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            return;
        }

        try {
            int idRecensione = Integer.parseInt(idRecensioneParam);
            RecensioneBean rec = recensioneDAO.doRetrieveById(idRecensione);
            if (rec == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Solo il proprietario o l'admin possono modificare
            if (!utente.isAdmin() && utente.getIdUtente() != rec.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            request.setAttribute("recensione", rec);
            request.getRequestDispatcher("/jsp/user/edit-recensione.jsp").forward(request, response);

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
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
        String descrizione = request.getParameter("descrizione");
        String valutazioneParam = request.getParameter("valutazione");

        if (idRecensioneParam == null || descrizione == null || valutazioneParam == null) {
            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
            return;
        }

        try {
            int idRecensione = Integer.parseInt(idRecensioneParam);
            double valutazione = Double.parseDouble(valutazioneParam);

            RecensioneBean rec = recensioneDAO.doRetrieveById(idRecensione);
            if (rec == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Solo il proprietario o l'admin possono modificare
            if (!utente.isAdmin() && utente.getIdUtente() != rec.getIdUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Aggiorna e salva
            rec.setDescrizione(descrizione.trim());
            rec.setValutazione(valutazione);
            recensioneDAO.doUpdate(rec);

            response.sendRedirect(request.getContextPath() + "/ProfiloServlet");

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
