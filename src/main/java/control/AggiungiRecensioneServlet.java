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

@WebServlet("/AggiungiRecensioneServlet")
public class AggiungiRecensioneServlet extends HttpServlet {
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

        // Se non è loggato o è admin, reindirizza al login
        if (utente == null || utente.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        String idProdottoParam = request.getParameter("idProdotto");
        String descrizione = request.getParameter("descrizione");
        String valutazioneParam = request.getParameter("valutazione");

        if (idProdottoParam != null && valutazioneParam != null && descrizione != null) {
            try {
                int idProdotto = Integer.parseInt(idProdottoParam);
                double valutazione = Double.parseDouble(valutazioneParam);

                RecensioneBean recensione = new RecensioneBean();
                recensione.setDescrizione(descrizione.trim());
                recensione.setValutazione(valutazione);
                recensione.setIdProdotto(idProdotto);
                recensione.setIdUtente(utente.getIdUtente());

                recensioneDAO.doSave(recensione);

                // Ricarica la pagina del prodotto con la nuova recensione visibile
                response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                return;
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
    }
}