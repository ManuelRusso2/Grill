package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import model.dao.impl.ProdottoDAOImpl;

@WebServlet("/AdminProdottoServlet")
public class AdminProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProdottoDAO prodottoDAO;

    @Override
    public void init() throws ServletException {
        // Inizializziamo il DAO dei prodotti per interagire con il database
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 1. L'admin deve poter gestire l'intero inventario
            // Recuperiamo tutti i prodotti (sia attivi che disattivi/nascosti)
            List<ProdottoBean> tuttiIProdotti = prodottoDAO.doRetrieveAllAdmin();
            
            // 2. Prepariamo i dati per la pagina di gestione
            request.setAttribute("prodottiAdmin", tuttiIProdotti);
            
            // 3. Inoltriamo la richiesta alla JSP del pannello amministrativo
            request.getRequestDispatcher("/jsp/admin/gestione-prodotti.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Errore 500: rimandiamo alla pagina di errore generica configurata nel web.xml
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recuperiamo l'azione da compiere (save, update, delete)
        String action = request.getParameter("action");

        try {
            if (action != null) {
                if ("save".equalsIgnoreCase(action)) {
                    // Creazione nuovo prodotto
                    ProdottoBean prodotto = leggiProdottoDaRequest(request, false);
                    prodottoDAO.doSave(prodotto);
                    
                } else if ("update".equalsIgnoreCase(action)) {
                    // Modifica prodotto esistente
                    ProdottoBean prodotto = leggiProdottoDaRequest(request, true);
                    prodottoDAO.doUpdate(prodotto);
                    
                } else if ("delete".equalsIgnoreCase(action)) {
                    // Eliminazione logica o fisica del prodotto tramite ID
                    int idProdotto = Integer.parseInt(request.getParameter("id"));
                    prodottoDAO.doDelete(idProdotto);
                }
            }

            // 2. Pattern POST-Redirect-GET: Reindirizziamo a questa servlet in GET 
            // per ricaricare la lista aggiornata ed evitare che un refresh del browser ri-esegua l'azione
            response.sendRedirect(request.getContextPath() + "/AdminProdottoServlet");
            
        } catch (NumberFormatException e) {
            // Se i parametri numerici (ID, costo, quantità) sono corrotti o vuoti
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    
    /**
     * METODO HELPER: Estrae i parametri HTTP inviati dal form e li mappa in un ProdottoBean.
     */
    private ProdottoBean leggiProdottoDaRequest(HttpServletRequest request, boolean conId) {
        ProdottoBean prodotto = new ProdottoBean();

        // Se stiamo modificando (update), dobbiamo obbligatoriamente recuperare la chiave primaria
        if (conId) {
            prodotto.setIdProdotto(Integer.parseInt(request.getParameter("id")));
        }

        prodotto.setNome(request.getParameter("nome"));
        prodotto.setDescrizione(request.getParameter("descrizione"));
        prodotto.setCosto(Double.parseDouble(request.getParameter("costo")));
        prodotto.setQuantita(Integer.parseInt(request.getParameter("quantita")));
        prodotto.setTipo(request.getParameter("tipo"));

        // GESTIONE STATO ATTIVO DINAMICO:
        // Spesso nei form si usa una checkbox per attivare/disattivare un prodotto.
        // Se la checkbox non è selezionata, il browser NON invia il parametro (sarà null).
        String attivoParam = request.getParameter("attivo");
        if (attivoParam != null) {
            // Gestisce sia stringhe come "true" sia il valore tipico delle checkbox "on"
            prodotto.setAttivo("true".equalsIgnoreCase(attivoParam) || "on".equalsIgnoreCase(attivoParam));
        } else {
            // Se il parametro non viene inviato, lo impostiamo a true di default
            prodotto.setAttivo(conId ? false : true); 
        }

        // Se il prodotto non appartiene a nessuna collezione particolare, impostiamo a null
        String idCollezione = request.getParameter("idCollezione");
        if (idCollezione != null && !idCollezione.trim().isEmpty()) {
            prodotto.setIdCollezione(Integer.parseInt(idCollezione.trim()));
        } else {
            prodotto.setIdCollezione(null);
        }

        return prodotto;
    }
}