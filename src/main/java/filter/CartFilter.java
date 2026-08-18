package filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
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
 * Il filtro si occupa di:
 *   Escludere le chiamate a risorse statiche (CSS, JS, immagini, font) per ottimizzare le prestazioni.</li>
 *   Distinguere tra utente registrato (il cui carrello risiede su DB) e utente non autenticato (il cui carrello è memorizzato in sessione).
 *   Calcolare il totale complessivo della quantità degli articoli presenti.
 *   Impostare l'attributo di richiesta {@code cartCount} per consentire all'interfaccia utente (es. Navbar/Badge) di mostrare il contatore aggiornato.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class CartFilter implements Filter {

    // Riferimenti ai DAO per la gestione della persistenza del carrello su DB
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    /**
     * Inizializza il filtro e crea le istanze concrete delle classi DAO necessarie.
     * Metodo eseguito dal Servlet Container durante la fase di startup del filtro.
     * 
     * @param filterConfig La configurazione fornita dal contenitore di Servlet
     * @throws ServletException Se si verifica un errore durante la fase di inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    /**
     * Intercetta la richiesta HTTP e calcola il conteggio totale dei prodotti nel carrello.
     * 
     * @param request  La richiesta {@link ServletRequest} in ingresso
     * @param response La risposta {@link ServletResponse} in uscita
     * @param chain    La catena di filtri {@link FilterChain}
     * @throws IOException      Se si verifica un errore di I/O durante l'elaborazione
     * @throws ServletException Se si verifica un errore di Servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI().toLowerCase();

        // =========================================================================
        // 1. ESCLUSIONE DELLE RISORSE STATICHE
        // Se la richiesta riguarda asset statici (CSS, JS, immagini, font), 
        // bypassa il calcolo del carrello per evitare query o operazioni inutili.
        // =========================================================================
        if (isStaticResource(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Recupero la sessione senza crearne una nuova (false)
        HttpSession session = httpRequest.getSession(false);
        int cartCount = 0;

        if (session != null) {
            UtenteBean utente = (UtenteBean) session.getAttribute("utente");

            // =========================================================================
            // 2. UTENTE REGISTRATO (E NON AMMINISTRATORE)
            // Estrae il carrello e i relativi elementi direttamente dal Database tramite DAO.
            // Somma la quantità totale (valori della mappa Mappa<Prodotto, Quantità>).
            // =========================================================================
            if (utente != null && !utente.isAdmin()) {
                try {
                    CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(utente.getIdUtente());
                    if (carrello != null) {
                        Map<ProdottoBean, Integer> prodottiInCarrello = 
                                contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
                        
                        if (prodottiInCarrello != null) {
                            cartCount = prodottiInCarrello.values().stream()
                                    .filter(Objects::nonNull)
                                    .mapToInt(Integer::intValue)
                                    .sum();
                        }
                    }
                } catch (SQLException e) {
                    // In caso di errore SQL, si traccia l'eccezione mantenendo il contatore a 0
                    e.printStackTrace();
                }
            } 
            // =========================================================================
            // 3. UTENTE OSPITE (GUEST / NON AUTENTICATO)
            // Calcola il conteggio leggendo l'oggetto CarrelloBean memorizzato in sessione.
            // =========================================================================
            else if (utente == null) {
                CarrelloBean carrelloSessione = (CarrelloBean) session.getAttribute("carrello");
                if (carrelloSessione != null && carrelloSessione.getProdotti() != null) {
                    cartCount = carrelloSessione.getProdotti().values().stream()
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();
                }
            }
        }

        // =========================================================================
        // 4. VALORIZZAZIONE DELL'ATTRIBUTO DI RICHIESTA E PROSEGUIMENTO
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
     * Metodo di supporto privato per determinare se un URI di richiesta punti a una risorsa statica.
     * 
     * @param uri L'URI della richiesta convertito in minuscolo
     * @return {@code true} se l'estensione corrisponde a un file statico, {@code false} altrimenti
     */
    private boolean isStaticResource(String uri) {
        return uri.endsWith(".css") || uri.endsWith(".js") 
            || uri.endsWith(".png") || uri.endsWith(".jpg") 
            || uri.endsWith(".jpeg") || uri.endsWith(".gif") 
            || uri.endsWith(".svg") || uri.endsWith(".ico") 
            || uri.endsWith(".woff") || uri.endsWith(".woff2") 
            || uri.endsWith(".ttf");
    }
}