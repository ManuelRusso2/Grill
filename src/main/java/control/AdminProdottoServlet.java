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
import model.bean.CategoriaBean;
import model.dao.ProdottoDAO;
import model.dao.CategoriaDAO;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.CategoriaDAOImpl;

@WebServlet("/AdminProdottoServlet")
public class AdminProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);

        if (session != null) {
            if (session.getAttribute("successMessage") != null) {
                request.setAttribute("successMessage", session.getAttribute("successMessage"));
                session.removeAttribute("successMessage");
            }
            if (session.getAttribute("errorMessage") != null) {
                request.setAttribute("errorMessage", session.getAttribute("errorMessage"));
                session.removeAttribute("errorMessage");
            }
        }

        String action = request.getParameter("action");

        try {
            if ("new".equalsIgnoreCase(action)) {
                List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                request.setAttribute("categorie", categorie);
                request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                return;
            }

            if ("edit".equalsIgnoreCase(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int idProdotto = Integer.parseInt(idParam);
                    ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

                    if (prodotto != null) {
                        request.setAttribute("prodotto", prodotto);
                        List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                        request.setAttribute("categorie", categorie);
                        request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                        return;
                    } else {
                        if (session != null) session.setAttribute("errorMessage", "Prodotto richiesto non trovato.");
                    }
                }
            }

            List<ProdottoBean> tuttiIProdotti = prodottoDAO.doRetrieveAllAdmin();
            request.setAttribute("prodottiAdmin", tuttiIProdotti);
            
            List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", categorie);

            synchronized (getServletContext()) {
                Object rawAlerts = getServletContext().getAttribute("adminAlerts");
                if (rawAlerts instanceof java.util.List<?>) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> adminAlerts = (java.util.List<String>) rawAlerts;
                    
                    if (!adminAlerts.isEmpty()) {
                        request.setAttribute("adminAlerts", new java.util.ArrayList<>(adminAlerts));
                        adminAlerts.clear();
                    }
                }
            }

            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            if (session != null) session.setAttribute("errorMessage", "ID prodotto non valido.");
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");

        try {
            if (action != null) {
                if ("save".equalsIgnoreCase(action)) {
                    ProdottoBean prodotto = leggiProdottoDaRequest(request, false);
                    prodottoDAO.doSave(prodotto);
                    session.setAttribute("successMessage", "Prodotto \"" + prodotto.getNome() + "\" inserito con successo!");

                } else if ("update".equalsIgnoreCase(action)) {
                    ProdottoBean prodotto = leggiProdottoDaRequest(request, true);
                    prodottoDAO.doUpdate(prodotto);
                    session.setAttribute("successMessage", "Prodotto ID #" + prodotto.getIdProdotto() + " aggiornato con successo!");

                } else if ("delete".equalsIgnoreCase(action)) {
                    int idProdotto = Integer.parseInt(request.getParameter("id"));
                    prodottoDAO.doDelete(idProdotto);
                    session.setAttribute("successMessage", "Prodotto ID #" + idProdotto + " disattivato/eliminato con successo.");
                }
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Errore nei dati inseriti: verifica che prezzo e quantità siano numeri validi.");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di persistenza nel Database: " + e.getMessage());
        }

        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        }
    }

    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) throws NumberFormatException {
        ProdottoBean prodotto = new ProdottoBean();

        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTaglie(request.getParameter("taglie"));

        String attivoParam = request.getParameter("attivo");
        prodotto.setAttivo(attivoParam != null && ("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam)));

        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        String[] idCategorie = request.getParameterValues("idCategoria");
        if (idCategorie != null) {
            List<CategoriaBean> categorie = new java.util.ArrayList<>();
            for (String idCat : idCategorie) {
                CategoriaBean cat = new CategoriaBean();
                cat.setIdCategoria(Integer.parseInt(idCat));
                categorie.add(cat);
            }
            prodotto.setCategorie(categorie);
        }

        // Pulisce il percorso immagine rimuovendo gli slash iniziali
        String immagine = request.getParameter("immagine");
        if (immagine != null && !immagine.trim().isEmpty()) {
            String cleanImg = immagine.trim();
            while (cleanImg.startsWith("/")) {
                cleanImg = cleanImg.substring(1);
            }
            prodotto.setImmagine(cleanImg);
        } else {
            prodotto.setImmagine("images/default.jpg");
        }

        return prodotto;
    }
}