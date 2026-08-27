package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.dao.CategoriaDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

/**
 * Servlet per la gestione del catalogo prodotti nell'area amministrativa.
 * Gestisce la visualizzazione del catalogo, l'apertura dei form di creazione e modifica,
 * il salvataggio, l'aggiornamento e la disattivazione/eliminazione dei prodotti,
 * oltre alla gestione degli alert di sistema a livello di ServletContext.
 */
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
            moveSessionAttributeToRequest(session, request, "successMessage");
            moveSessionAttributeToRequest(session, request, "errorMessage");
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                
                case "new":
                    request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
                    request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                    return;

                case "edit":
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        int idProdotto = Integer.parseInt(idParam.trim());
                        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

                        if (prodotto != null) {
                            request.setAttribute("prodotto", prodotto);
                            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
                            request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                            return;
                        } else if (session != null) {
                            session.setAttribute("errorMessage", "Prodotto richiesto non trovato.");
                        }
                    }
                    break;

                default:
                    break;
            }

            request.setAttribute("prodottiAdmin", prodottoDAO.doRetrieveAllAdmin());
            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
            processAdminAlerts(request);

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
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                
                case "save":
                    ProdottoBean nuovoProdotto = leggiProdottoDaRequest(request, false);
                    prodottoDAO.doSave(nuovoProdotto);
                    session.setAttribute("successMessage", "Prodotto \"" + nuovoProdotto.getNome() + "\" inserito con successo!");
                    break;

                case "update":
                    ProdottoBean prodottoAggiornato = leggiProdottoDaRequest(request, true);
                    prodottoDAO.doUpdate(prodottoAggiornato);
                    session.setAttribute("successMessage", "Prodotto ID #" + prodottoAggiornato.getIdProdotto() + " aggiornato con successo!");
                    break;

                case "delete":
                    int idProdotto = Integer.parseInt(request.getParameter("id"));
                    prodottoDAO.doDelete(idProdotto);
                    session.setAttribute("successMessage", "Prodotto ID #" + idProdotto + " disattivato/eliminato con successo.");
                    break;

                default:
                    break;
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Errore nei dati inseriti: verifica che prezzo, IVA e quantità siano numeri validi.");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di persistenza nel Database: " + e.getMessage());
        }

        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        }
    }

    private void moveSessionAttributeToRequest(HttpSession session, HttpServletRequest request, String attributeName) {
        if (session.getAttribute(attributeName) != null) {
            request.setAttribute(attributeName, session.getAttribute(attributeName));
            session.removeAttribute(attributeName);
        }
    }

    private void processAdminAlerts(HttpServletRequest request) {
        synchronized (getServletContext()) {
            @SuppressWarnings("unchecked")
            List<String> alerts = (List<String>) getServletContext().getAttribute("adminAlerts");

            if (alerts != null && !alerts.isEmpty()) {
                request.setAttribute("adminAlerts", new ArrayList<>(alerts));
                alerts.clear();
            }
        }
    }

    /**
     * Mappa ed estrae i parametri inviati nel form della Request per costruire un oggetto ProdottoBean.
     */
    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) throws NumberFormatException {
        ProdottoBean prodotto = new ProdottoBean();

        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        
        // Estrazione e gestione del campo IVA
        String ivaParam = request.getParameter("iva");
        if (ivaParam != null && !ivaParam.trim().isEmpty()) {
            prodotto.setIva(Double.parseDouble(ivaParam.trim()));
        } else {
            prodotto.setIva(22.0); // Valore di default standard IVA 22%
        }

        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTaglie(request.getParameter("taglie"));

        String attivoParam = request.getParameter("attivo");
        prodotto.setAttivo("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam));

        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        String[] idCategorie = request.getParameterValues("idCategoria");
        if (idCategorie != null) {
            List<CategoriaBean> categorie = new ArrayList<>();
            for (String idCat : idCategorie) {
                CategoriaBean cat = new CategoriaBean();
                cat.setIdCategoria(Integer.parseInt(idCat));
                categorie.add(cat);
            }
            prodotto.setCategorie(categorie);
        }

        String immagine = request.getParameter("immagine");
        if (immagine != null && !immagine.trim().isEmpty()) {
            prodotto.setImmagine(immagine.trim().replaceAll("^/+", ""));
        } else {
            prodotto.setImmagine("images/default.jpg");
        }

        return prodotto;
    }
}