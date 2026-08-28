package filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import model.bean.CategoriaBean;
import model.dao.CategoriaDAO;
import model.dao.impl.CategoriaDAOImpl;

/**
 * CategorieFilter
 * Filtro globale applicato a tutte le rotte dell'applicazione per garantire 
 * la costante disponibilità dell'elenco delle categorie.
 * 
 * Il filtro popola l'attributo di richiesta {@code categorie} caricando i dati dal database tramite DAO.
 * Questo consente alla barra di navigazione/header dinamica, presente in tutte le viste JSP, 
 * di renderizzare correttamente i menu a discesa delle categorie senza dover replicare 
 * la logica di recupero all'interno di ogni singola Servlet controller.
 * 
 * Per ottimizzare le prestazioni:
 *   Esclude le richieste dirette ad asset statici (CSS, JS, immagini, font).
 *   Evita letture ridondanti dal DB se l'attributo è già stato valorizzato nella richiesta corrente.
 */
@WebFilter("/*")
public class CategorieFilter implements Filter {

    // Riferimento all'interfaccia DAO per l'accesso ai dati delle categorie
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializza il filtro instanziando l'implementazione del DAO per le categorie.
     * Invocato dal Servlet Container durante la fase di avvio del filtro.
     * 
     * @param filterConfig La configurazione del filtro fornita dal Servlet Container
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Intercetta le richieste HTTP in ingresso per verificare ed eventualmente iniettare
     * la lista delle categorie nell'ambito della richiesta (Request Scope).
     * 
     * @param request  L'oggetto {@link ServletRequest} generico
     * @param response L'oggetto {@link ServletResponse} generico
     * @param chain    La catena dei filtri {@link FilterChain} per proseguire l'esecuzione
     * @throws IOException      Se si verifica un errore di I/O
     * @throws ServletException Se si verifica un errore a livello di Servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Conversione della richiesta generica nell'interfaccia HTTP per accedere all'URI
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()).toLowerCase();

        // =========================================================================
        // 1. FILTRAGGIO DELLE RISORSE STATICHE
        // Se la richiesta è diretta ad un file statico (CSS, JS, immagini, font),
        // ignora la logica di recupero categorie per evitare query SQL non necessarie.
        // =========================================================================
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // =========================================================================
        // 2. VERIFICA E CARICAMENTO DELLE CATEGORIE
        // Esegue la query su DB soltanto se l'attributo "categorie" non è ancora presente
        // nella Request (es. evita re-query in caso di inoltri con Forward).
        // =========================================================================
        if (request.getAttribute("categorie") == null) {
            try {
                // Interroga il DAO per recuperare tutte le categorie memorizzate
                List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
                
                // Imposta la lista trovata oppure un ripiego (fallback) lista vuota se null
                request.setAttribute("categorie", (categorie != null) ? categorie : new ArrayList<>());
            } catch (SQLException e) {
                // =========================================================================
                // 3. GESTIONE DELL'ECCEZIONE SQL
                // Traccia l'errore nei log di sistema e imposta una lista vuota per evitare
                // eccezioni NullPointerException durante la resa delle JSP.
                // =========================================================================
                e.printStackTrace();
                request.setAttribute("categorie", new ArrayList<CategoriaBean>());
            }
        }
        
        // =========================================================================
        // 4. PROSEGUIMENTO DELLA CATENA DI FILTRI
        // Inoltra la richiesta al filtro successivo o alla risorsa di destinazione (Servlet/JSP)
        // =========================================================================
        chain.doFilter(request, response);
    }

    /**
     * Pulisce ed eventualmente rilascia le risorse allocate dal filtro.
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
            || path.endsWith(".ico"); // Per il favicon nella root
    }
}