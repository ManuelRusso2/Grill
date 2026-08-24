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

import model.bean.AcquistoBean;
import model.bean.CategoriaBean;
import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.CategoriaDAO;
import model.dao.RecensioneDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.RecensioneDAOImpl;

/**
 * ProfiloServlet
 * Servlet controller responsabile della gestione e della visualizzazione dell'area personale dell'utente.
 * Recupera ed elabora le seguenti informazioni riservate all'utente autenticato:
 *   Elenco delle categorie di prodotti (necessario per l'header/menu di navigazione).
 *   Storico degli ordini/acquisti effettuati.
 *   Elenco delle recensioni scritte dall'utente.
 *   
 * Inoltre, applica le corrette direttive di sicurezza HTTP anti-caching per proteggere la privacy dei dati personali.
 */
@WebServlet("/ProfiloServlet")
public class ProfiloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti alle interfacce DAO per l'accesso e l'estrazione dei dati dal DB
    private AcquistoDAO acquistoDAO;
    private CategoriaDAO categoriaDAO;
    private RecensioneDAO recensioneDAO;

    /**
     * Inizializza la servlet e istanzia le relative implementazioni dei Data Access Object (DAO).
     * Questo metodo viene richiamato una sola volta dal Servlet Container durante la fase di init.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione delle risorse
     */
    @Override
    public void init() throws ServletException {
        this.acquistoDAO = new AcquistoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per la visualizzazione del profilo.
     * Verifica l'autenticazione dell'utente, previene la memorizzazione in cache dei dati sensibili,
     * recupera dal database gli ordini e le recensioni dell'utente e reindirizza alla JSP {@code profilo.jsp}.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} per recuperare la sessione e passare i dati alla vista
     * @param response L'oggetto {@link HttpServletResponse} per la gestione degli header HTTP e dei reindirizzamenti
     * @throws ServletException Se si verifica un errore durante il forwarding alla vista JSP
     * @throws IOException      Se si verifica un errore di I/O durante il redirect o l'invio del codice di errore HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // =========================================================================
        // 1. CONTROLLO DI AUTENTICAZIONE UTENTE
        // Recupera la sessione corrente senza instanziarne una nuova (false).
        // Se l'utente non è autenticato, viene reindirizzato alla pagina di login.
        // =========================================================================
        HttpSession session = request.getSession(false);
        UtenteBean utente = null;
        if (session != null) {
            utente = (UtenteBean) session.getAttribute("utente");
        }
        
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return; // Interrompe l'esecuzione del metodo
        }

        // =========================================================================
        // 2. SICUREZZA E PRIVACY: PREVENZIONE CACHING
        // Configura gli header della risposta HTTP per impedire al browser o ai proxy
        // di salvare in cache le pagine contenenti dati personali dell'utente.
        // =========================================================================
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // Standard HTTP 1.1
        response.setHeader("Pragma", "no-cache");                                   // Standard HTTP 1.0
        response.setDateHeader("Expires", 0);                                       // Scadenza immediata per i Proxy

        try {
            // =========================================================================
            // 3. CARICAMENTO CATEGORIE PRODOTTO
            // Estrae tutte le categorie disponibili per consentire la corretta
            // renderizzazione del menu di navigazione dinamico nell'header.
            // =========================================================================
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);
            
            // =========================================================================
            // 4. RECUPERO STORICO ACQUISTI
            // Estrae dal database la lista degli acquisti registrati a nome dell'utente loggato
            // =========================================================================
            List<AcquistoBean> acquisti = acquistoDAO.doRetrieveByUtente(utente.getIdUtente());
            request.setAttribute("acquisti", acquisti);
            
            // =========================================================================
            // 5. RECUPERO RECENSIONI UTENTE
            // Estrae dal database tutte le recensioni rilasciate in precedenza dall'utente
            // =========================================================================
            List<RecensioneBean> recensioniUtente = recensioneDAO.doRetrieveByUtente(utente.getIdUtente());
            request.setAttribute("recensioniUtente", recensioniUtente);

            // =========================================================================
            // 6. INOLTRO ALLA VISTA (JSP)
            // Trasferisce il controllo alla pagina 'profilo.jsp' per la visualizzazione dei dati
            // =========================================================================
            request.getRequestDispatcher("/jsp/common/profilo.jsp").forward(request, response);
            
        } catch (SQLException e) {
            // Log dell'eccezione SQL lato server per scopi di tracciamento ed errori
            e.printStackTrace();
            
            // Invia un codice di errore HTTP 500 (Internal Server Error) al client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati del profilo.");
        }
    }

    /**
     * Gestisce le richieste HTTP POST delegando l'elaborazione al metodo {@link #doGet}.
     * Garantisce la coerente visualizzazione della pagina del profilo anche in caso di invio via POST.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore durante il forwarding
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delega direttamente la gestione al metodo doGet per riutilizzare la stessa logica
        doGet(request, response);
    }
}