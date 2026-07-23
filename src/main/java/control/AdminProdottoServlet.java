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

    /**
     * GET: Gestisce sia la visualizzazione dell'inventario che l'apertura del form in modalità modifica (edit)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);

        // 1. Recupero e pulizia dei Flash Messages salvati in sessione durante la POST
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
            // 2. Se l'azione è "new", andiamo direttamente al form di creazione con le categorie
            if ("new".equalsIgnoreCase(action)) {
                List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                request.setAttribute("categorie", categorie);
                request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                return;
            }

            // 3. Se l'azione è "edit", recuperiamo il singolo prodotto e andiamo al form di modifica
            if ("edit".equalsIgnoreCase(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int idProdotto = Integer.parseInt(idParam);
                    ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

                    if (prodotto != null) {
                        request.setAttribute("prodotto", prodotto);
                        // Carica le categorie per il select nel form
                        List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                        request.setAttribute("categorie", categorie);
                        request.getRequestDispatcher("/jsp/admin/nuovo-prodotto.jsp").forward(request, response);
                        return;
                    } else {
                        if (session != null) session.setAttribute("errorMessage", "Prodotto richiesto non trovato.");
                    }
                }
            }

            // 4. Flusso standard: recupero di tutto l'inventario (attivi e disattivi)
            List<ProdottoBean> tuttiIProdotti = prodottoDAO.doRetrieveAllAdmin();
            request.setAttribute("prodottiAdmin", tuttiIProdotti);
            
            // Carica le categorie per il select nel form (quando creo un nuovo prodotto)
            List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", categorie);
            // Recupero eventuali alert amministrativi generati da altre operazioni (es. prodotto esaurito)
            java.util.List<String> adminAlerts = (java.util.List<String>) getServletContext().getAttribute("adminAlerts");
            if (adminAlerts != null && !adminAlerts.isEmpty()) {
                request.setAttribute("adminAlerts", adminAlerts);
                // Rimuoviamo gli alert dal contesto dopo averli passati alla request per evitare ripetizioni
                getServletContext().removeAttribute("adminAlerts");
            }
            // 5. Inoltro alla tabella di gestione prodotti
            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            if (session != null) session.setAttribute("errorMessage", "ID prodotto non valido.");
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST: Gestisce le operazioni di scrittura nel DB (save, update, delete) applicando il pattern PRG
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");

        try {
            if (action != null) {
                // Gestioni prodotti normali
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

        // 1.b Recupero eventuali alert amministrativi generati da altre operazioni (es. prodotto esaurito)
        java.util.List<String> adminAlerts = (java.util.List<String>) getServletContext().getAttribute("adminAlerts");
        if (adminAlerts != null && !adminAlerts.isEmpty()) {
            request.setAttribute("adminAlerts", adminAlerts);
            // Rimuoviamo gli alert dal contesto dopo averli passati alla request per evitare ripetizioni
            getServletContext().removeAttribute("adminAlerts");
        }

        // Redirect in GET alla Servlet per ricaricare la vista ed evitare doppi invii
        // (se non è stata già mandata una risposta AJAX)
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
        }
    }

    /**
     * METODO HELPER: Estrae e valida i parametri HTTP inviati dai form per costruire un ProdottoBean
     */
    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) throws NumberFormatException {
        ProdottoBean prodotto = new ProdottoBean();

        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTipo(request.getParameter("tipo"));

        // Gestione checkbox/stato attivo
        String attivoParam = request.getParameter("attivo");
        if (attivoParam != null) {
            prodotto.setAttivo("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam));
        } else {
            prodotto.setAttivo(!conId); 
        }

        // Gestione ID Collezione opzionale
        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        return prodotto;
    }
}