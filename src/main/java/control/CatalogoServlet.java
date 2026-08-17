package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CarrelloBean;
import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.CarrelloDAO;
import model.dao.CategoriaDAO;
import model.dao.ContenutoDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.ContenutoDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

/**
 * Servlet per la gestione e visualizzazione del catalogo prodotti.
 * Mappa la richiesta alla rotta '/CatalogoServlet'.
 */
@WebServlet("/CatalogoServlet")
public class CatalogoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti alle interfacce Data Access Object (DAO)
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    /**
     * Inizializza le implementazioni dei DAO al momento dell'avvio della Servlet.
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per il recupero e la visualizzazione del catalogo.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        try {
            // -----------------------------------------------------------------
            // 1. Caricamento categorie per il menu di navigazione
            // -----------------------------------------------------------------
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);

            // -----------------------------------------------------------------
            // 2. Filtro prodotti in base alla categoria selezionata (se presente)
            // -----------------------------------------------------------------
            String categoriaParam = getTrimmedParam(request, "categoria");
            List<ProdottoBean> prodotti;

            if (categoriaParam != null) {
                try {
                    // Tenta il parsing dell'ID categoria
                    int idCategoria = Integer.parseInt(categoriaParam);
                    
                    // Recupera i prodotti specifici per quella categoria
                    prodotti = prodottoDAO.doRetrieveByCategoria(idCategoria);
                    
                    // Recupera le informazioni della categoria selezionata per la UI
                    CategoriaBean categoriaAttiva = categoriaDAO.doRetrieveById(idCategoria);
                    request.setAttribute("categoriaAttiva", categoriaAttiva);
                } catch (NumberFormatException e) {
                    // Fallback: se l'ID non è un numero valido, carica l'intero catalogo
                    prodotti = prodottoDAO.doRetrieveAllClientiRaggruppati();
                }
            } else {
                // Nessun filtro specificato: carica il catalogo prodotti completo
                prodotti = prodottoDAO.doRetrieveAllClientiRaggruppati();
            }
            
            // Passa la lista dei prodotti alla vista JSP
            request.setAttribute("prodotti", prodotti);

            // -----------------------------------------------------------------
            // 3. Gestione ruolo utente e calcolo badge carrello (se loggato)
            // -----------------------------------------------------------------
            HttpSession session = request.getSession(false); // Recupera la sessione esistente senza crearne una nuova
            if (session != null) {
                UtenteBean utente = (UtenteBean) session.getAttribute("utente");
                if (utente != null) {
                    // Imposta il flag per distinguere la vista Amministratore / Cliente
                    request.setAttribute("isAdmin", utente.isAdmin());

                    // Se l'utente è un cliente standard, calcola il conteggio prodotti per il badge carrello
                    if (!utente.isAdmin()) {
                        int cartCount = calcolaConteggioCarrello(utente.getIdUtente());
                        request.setAttribute("cartCount", cartCount);
                    }
                }
            }

            // -----------------------------------------------------------------
            // 4. Inoltro (forward) della richiesta alla pagina JSP del catalogo
            // -----------------------------------------------------------------
            request.getRequestDispatcher("/jsp/common/catalogo.jsp").forward(request, response);

        } catch (SQLException e) {
            // Log dell'eccezione SQL e invio risposta HTTP 500 in caso di errore DB
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reindirizza le richieste POST al metodo doGet per garantire lo stesso comportamento.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    // =========================================================================
    // METODI DI SUPPORTO (HELPER METHODS)
    // =========================================================================

    /**
     * Calcola il numero totale di elementi (quantità sum) presenti nel carrello di un utente.
     * 
     * @param idUtente L'identificativo dell'utente nel sistema
     * @return Il totale dei pezzi presenti nel carrello, 0 se il carrello è vuoto o inesistente
     * @throws SQLException Se si verifica un errore durante l'interrogazione del DB
     */
    private int calcolaConteggioCarrello(int idUtente) throws SQLException {
        // Recupera il carrello associato all'utente
        CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        if (carrello == null) {
            return 0;
        }

        // Recupera la mappa <Prodotto, Quantità> contenuta nel carrello
        Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
        if (prodottiInCarrello == null) {
            return 0;
        }

        // Somma le quantità di tutti i prodotti presenti nel carrello
        int totalItems = 0;
        for (Integer quantita : prodottiInCarrello.values()) {
            if (quantita != null) {
                totalItems += quantita;
            }
        }
        return totalItems;
    }

    /**
     * Estrae un parametro dalla richiesta HTTP eliminando gli spazi bianchi iniziali e finali.
     * 
     * @param request La richiesta HTTP corrente
     * @param name Il nome del parametro da estrarre
     * @return La stringa pulita, oppure null se il parametro è assente o vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}