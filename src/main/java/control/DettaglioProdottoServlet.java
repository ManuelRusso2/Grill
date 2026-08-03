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

import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.bean.CategoriaBean;
import model.bean.RecensioneBean;
import model.dao.ProdottoDAO;
import model.dao.CategoriaDAO;
import model.dao.RecensioneDAO;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.RecensioneDAOImpl;

@WebServlet("/DettaglioProdottoServlet")
public class DettaglioProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;
    private RecensioneDAO recensioneDAO;

    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
            return;
        }

        try {
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);
            
            int idProdotto = Integer.parseInt(idParam);
            ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
            
            if (prodotto == null || !prodotto.isAttivo()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            request.setAttribute("prodotto", prodotto);

            // Carica le varianti
            String nomeBase = prodotto.getNome().contains(" - ")
                ? prodotto.getNome().substring(0, prodotto.getNome().lastIndexOf(" - "))
                : prodotto.getNome();
            List<ProdottoBean> varianti = prodottoDAO.doRetrieveVarianti(nomeBase);
            if (varianti.size() > 1) {
                request.setAttribute("varianti", varianti);
                request.setAttribute("nomeBase", nomeBase);
            }
            
            // --- RECUPERO RECENSIONI ---
            List<RecensioneBean> recensioni = recensioneDAO.doRetrieveByProdotto(idProdotto);
            request.setAttribute("recensioni", recensioni);

            // Controlla se l'utente è admin
            HttpSession session = request.getSession(false);
            if (session != null) {
                UtenteBean utente = (UtenteBean) session.getAttribute("utente");
                if (utente != null) {
                    request.setAttribute("isAdmin", utente.isAdmin());
                }
            }
            
            request.getRequestDispatcher("/jsp/common/dettaglio-prodotto.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}