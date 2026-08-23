package filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.bean.CarrelloBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;

/**
 * CartFilter
 * Filtro globale applicato a tutte le rotte per il calcolo in tempo reale del conteggio degli elementi nel carrello.
 * 
 * Il filtro si occupa di:
 *   - Escludere le chiamate a risorse statiche (CSS, JS, immagini, favicon) per ottimizzare le prestazioni.
 *   - Calcolare il carrello da Database esclusivamente per gli utenti autenticati che non sono amministratori.
 *   - Sommare la quantità totale di tutti i prodotti presenti a carrello.
 *   - Impostare l'attributo di richiesta {@code cartCount} per consentire all'interfaccia utente (es. Navbar/Badge) di mostrare il contatore aggiornato.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class CartFilter implements Filter {

    // Riferimenti ai DAO per la gestione della persistenza del carrello su DB
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    /**
     * Inizializza il filtro instanziando le implementazioni concrete dei DAO per l'accesso ai dati.
     * Invocato dal Servlet Container durante la fase di avvio del filtro.
     * 
     * @param filterConfig La configurazione del filtro fornita dal Servlet Container
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    /**
     * Intercetta la richiesta HTTP in ingresso e calcola il conteggio totale dei prodotti nel carrello.
     * 
     * @param request  La richiesta {@link ServletRequest} in ingresso
     * @param response La risposta {@link ServletResponse} in uscita
     * @param chain    La catena di filtri {@link FilterChain} per proseguire l'esecuzione
     * @throws IOException      Se si verifica un errore di I/O durante l'elaborazione
     * @throws ServletException Se si verifica un errore a livello di Servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()).toLowerCase();

        // =========================================================================
        // 1. ESCLUSIONE DELLE RISORSE STATICHE
        // Se la richiesta riguarda asset statici (CSS, JS, immagini, favicon), 
        // bypassa il calcolo del carrello per evitare query SQL non necessarie.
        // =========================================================================
        if (isStaticResource(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Recupero della sessione senza crearne una nuova se non esiste
        HttpSession session = httpRequest.getSession(false);
        int cartCount = 0;

        // =========================================================================
        // 2. UTENTE REGISTRATO (E NON AMMINISTRATORE)
        // Se l'utente è autenticato ed è un cliente, recupera il carrello dal DB
        // e calcola la somma totale delle quantità di tutti i prodotti presenti.
        // =========================================================================
        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");

            if (utente != null && !utente.isAdmin()) {
                try {
                    CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(utente.getIdUtente());
                    if (carrello != null) {
                        Map<ProdottoBean, Integer> prodottiInCarrello = 
                                contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
                        
                        if (prodottiInCarrello != null) {
                            for (Integer qta : prodottiInCarrello.values()) {
                                if (qta != null) {
                                    cartCount += qta;
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    // In caso di errore SQL, traccia l'eccezione mantenendo il contatore a 0
                    e.printStackTrace();
                }
            }
        }

        // =========================================================================
        // 3. VALORIZZAZIONE DELL'ATTRIBUTO DI RICHIESTA E PROSEGUIMENTO
        // Rende il numero totale di elementi accessibile nelle pagine JSP (es. ${cartCount})
        // =========================================================================
        httpRequest.setAttribute("cartCount", cartCount);
        chain.doFilter(request, response);
    }

    /**
     * Rilascia eventuali risorse allocate dal filtro alla sua dismissione.
     */
    @Override
    public void destroy() {
        // Nessuna risorsa esplicita da rilasciare
    }

    /**
     * Metodo ausiliario di controllo per verificare se il percorso richiesto appartiene
     * ad una risorsa statica o ad una cartella di asset pubblici.
     * 
     * @param path Il percorso relativo della richiesta HTTP in minuscolo
     * @return {@code true} se il percorso identifica un file statico, {@code false} altrimenti
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") 
            || path.startsWith("/js/") 
            || path.startsWith("/images/")
            || path.endsWith(".ico");
    }
}