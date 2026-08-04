package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.UtenteBean;
import model.bean.RecensioneBean;
import model.bean.CategoriaBean;
import model.dao.RecensioneDAO;
import model.dao.UtenteDAO;
import model.dao.CategoriaDAO;
import model.dao.impl.RecensioneDAOImpl;
import model.dao.impl.UtenteDAOImpl;
import model.dao.impl.CategoriaDAOImpl;

@WebServlet("/AdminRecensioniServlet")
public class AdminRecensioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RecensioneDAO recensioneDAO;
    private UtenteDAO utenteDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Sicurezza: Solo gli amministratori possono accedere
        if (utente == null || !utente.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            // 0. Carica tutte le categorie per il menu globale
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);

            // 1. Carica la lista di tutti gli utenti per popolare la select del filtro
            List<UtenteBean> tuttiUtenti = utenteDAO.doRetrieveAllClienti();
            request.setAttribute("tuttiUtenti", tuttiUtenti);

            // 2. Controllo filtro utente
            String idUtenteParam = request.getParameter("idUtente");
            List<RecensioneBean> tuteRecensioni;

            if (idUtenteParam != null && !idUtenteParam.trim().isEmpty()) {
                int idUtente = Integer.parseInt(idUtenteParam);
                tuteRecensioni = recensioneDAO.doRetrieveByUtente(idUtente);
            } else {
                tuteRecensioni = recensioneDAO.doRetrieveAll();
            }

            request.setAttribute("tuteRecensioni", tuteRecensioni);
            request.getRequestDispatcher("/jsp/admin/gestione-recensioni.jsp").forward(request, response);

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        if (utente == null || !utente.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        
        if ("delete".equals(action)) {
            String idRecensioneParam = request.getParameter("idRecensione");
            String idUtenteParam = request.getParameter("idUtente");
            
            if (idRecensioneParam != null) {
                try {
                    int idRecensione = Integer.parseInt(idRecensioneParam);
                    recensioneDAO.doDelete(idRecensione);

                    // Se stavamo filtrando, manteniamo il filtro dopo l'eliminazione
                    String redirectUrl = request.getContextPath() + "/AdminRecensioniServlet";
                    if (idUtenteParam != null && !idUtenteParam.trim().isEmpty()) {
                        redirectUrl += "?idUtente=" + idUtenteParam;
                    }
                    
                    response.sendRedirect(redirectUrl);
                    return;
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/AdminRecensioniServlet");
    }
}